#!/usr/bin/env python3
from __future__ import annotations

import argparse
import concurrent.futures
import json
import os
import re
import shutil
import subprocess
import tempfile
import textwrap
import threading
import time
from pathlib import Path
from typing import Any

SCRIPT_DIR = Path(__file__).resolve().parent
CATALOG_PATH = SCRIPT_DIR.parent / "templates.json"
DEFAULT_MODEL = "gemini-3.1-flash-tts-preview"


def load_catalog() -> dict[str, Any]:
    with CATALOG_PATH.open(encoding="utf-8") as file:
        return json.load(file)


def resolve_model() -> str:
    explicit = os.environ.get("GEMINI_TTS_MODEL")
    if explicit:
        return explicit
    legacy = os.environ.get("GEMINI_FLASH_DEFAULT")
    if legacy and "tts" in legacy.lower():
        return legacy
    return DEFAULT_MODEL


def resolve_api_key() -> str:
    for name in ("GEMINI_API_KEY", "GOOGLE_API_KEY", "OPENCODE_GOOGLE_API_KEY"):
        value = os.environ.get(name, "").strip()
        if value:
            return value
    raise RuntimeError("Set GEMINI_API_KEY or GOOGLE_API_KEY before generating audio.")


def compile_notes(template: dict[str, Any]) -> str | None:
    if template.get("notes"):
        return str(template["notes"])
    parts = [
        f"{label}: {template[key]}"
        for label, key in (("Style", "style"), ("Pace", "pace"), ("Accent", "accent"))
        if template.get(key)
    ]
    return ". ".join(parts) or None


def resolve_args(
    args: argparse.Namespace, catalog: dict[str, Any]
) -> tuple[str, str | None, str | None, str | None, float]:
    template = None
    template_name = args.template or catalog.get("defaultTemplate")
    if template_name:
        template = next(
            (item for item in catalog["templates"] if item["id"] == template_name),
            None,
        )
        if not template:
            raise ValueError(
                f"Template '{template_name}' not found. Use --list-templates."
            )

    voice = template.get("voice") if template else None
    profile = template.get("profile") if template else None
    scene = template.get("scene") if template else None
    notes = compile_notes(template) if template else None
    speed = template.get("speed") if template else None

    voice = args.voice if args.voice is not None else voice
    profile = args.profile if args.profile is not None else profile
    scene = args.scene if args.scene is not None else scene
    notes = args.notes if args.notes is not None else notes
    speed = args.speed if args.speed is not None else speed

    resolved_speed = float(speed if speed is not None else 1.0)
    if not 0.5 <= resolved_speed <= 2.0:
        raise ValueError("Speed must be between 0.5 and 2.0.")
    return voice or "Orus", profile, scene, notes, resolved_speed


def list_voices(catalog: dict[str, Any]) -> None:
    print("Available voices:")
    for voice in catalog["voices"]:
        print(
            f"  {voice['name']:<16} style={voice.get('style', '-'):<22} "
            f"pitch={voice.get('pitch', '-')}"
        )


def list_templates(catalog: dict[str, Any]) -> None:
    print("Available templates:")
    default_template = catalog.get("defaultTemplate")
    for template in catalog["templates"]:
        marker = " (default)" if template["id"] == default_template else ""
        print(
            f"  {template['id']:<24} {template['label']:<40} "
            f"voice={template.get('voice', 'Orus')}{marker}"
        )


def show_template(catalog: dict[str, Any], name: str) -> None:
    template = next(
        (item for item in catalog["templates"] if item["id"] == name), None
    )
    if not template:
        raise ValueError(f"Template '{name}' not found.")
    print(json.dumps(template, indent=2, ensure_ascii=False))


def chunk_text(text: str, max_chars: int = 800) -> list[str]:
    paragraphs = [paragraph.strip() for paragraph in text.split("\n\n") if paragraph.strip()]
    chunks: list[str] = []
    current = ""
    for paragraph in paragraphs:
        candidate = f"{current}\n\n{paragraph}" if current else paragraph
        if len(candidate) <= max_chars:
            current = candidate
            continue
        if current:
            chunks.append(current)
        if len(paragraph) > max_chars:
            wrapped = textwrap.wrap(
                paragraph,
                width=max_chars,
                break_long_words=False,
                break_on_hyphens=False,
            )
            chunks.extend(wrapped[:-1])
            current = wrapped[-1]
        else:
            current = paragraph
    if current:
        chunks.append(current)
    if not chunks:
        raise ValueError("Input text is empty.")
    return chunks


class RequestRateLimiter:
    def __init__(self, requests_per_minute: float):
        self.minimum_interval = (
            60.0 / requests_per_minute if requests_per_minute > 0 else 0.0
        )
        self.lock = threading.Lock()
        self.next_request_time = 0.0

    def wait(self) -> None:
        if self.minimum_interval <= 0:
            return
        with self.lock:
            now = time.monotonic()
            delay = self.next_request_time - now
            if delay > 0:
                time.sleep(delay)
                now = time.monotonic()
            self.next_request_time = now + self.minimum_interval


def safe_error_summary(error: Exception) -> str:
    parts = [error.__class__.__name__]
    status = getattr(error, "status_code", None)
    if isinstance(status, int):
        parts.append(f"status={status}")
    code = getattr(error, "code", None)
    if isinstance(code, str) and re.fullmatch(r"[A-Za-z0-9_.-]{1,64}", code):
        parts.append(f"code={code}")
    return " ".join(parts)


def retry_delay_seconds(error: Exception) -> float:
    message = str(error)
    for pattern in (
        r"retryDelay['\"]?: ['\"]?(\d+(?:\.\d+)?)s",
        r"retry in (\d+(?:\.\d+)?)s",
    ):
        match = re.search(pattern, message, re.IGNORECASE)
        if match:
            return float(match.group(1)) + 2.0
    return 60.0


def process_chunk(
    index: int,
    text: str,
    total: int,
    voice: str,
    profile: str | None,
    scene: str | None,
    notes: str | None,
    context: str | None,
    model: str,
    api_key: str,
    limiter: RequestRateLimiter | None,
) -> tuple[int, bytes | None, int, Exception | None]:
    from google import genai
    from google.genai import types

    print(f"Starting chunk {index + 1}/{total}...")
    prompt_sections = []
    if profile:
        prompt_sections.append(f"# AUDIO PROFILE\n{profile}")
    if scene:
        prompt_sections.append(f"## SCENE\n{scene}")
    if notes:
        prompt_sections.append(f"## DIRECTOR NOTES\n{notes}")
    if context:
        prompt_sections.append(f"## CONTEXT\n{context}")
    prompt_sections.append(f"## TRANSCRIPT\n{text}")
    prompt = "\n\n".join(prompt_sections)

    client = genai.Client(api_key=api_key)
    config = types.GenerateContentConfig(
        temperature=1,
        response_modalities=["audio"],
        speech_config=types.SpeechConfig(
            voice_config=types.VoiceConfig(
                prebuilt_voice_config=types.PrebuiltVoiceConfig(voice_name=voice)
            )
        ),
    )

    for attempt in range(3):
        try:
            if limiter:
                limiter.wait()
            response = client.models.generate_content(
                model=model,
                contents=[
                    types.Content(
                        role="user", parts=[types.Part.from_text(text=prompt)]
                    )
                ],
                config=config,
            )
            parts = getattr(response, "parts", None)
            if not parts:
                candidates = getattr(response, "candidates", None) or []
                if candidates:
                    parts = getattr(candidates[0].content, "parts", None)

            pcm = bytearray()
            sample_rate = 24000
            for part in parts or []:
                inline = getattr(part, "inline_data", None)
                if not inline or not inline.data:
                    continue
                pcm.extend(inline.data)
                match = re.search(r"rate=(\d+)", inline.mime_type or "")
                if match:
                    sample_rate = int(match.group(1))
            if not pcm:
                raise RuntimeError("Gemini returned no audio data.")
            print(f"Finished chunk {index + 1}/{total}")
            return index, bytes(pcm), sample_rate, None
        except Exception as error:
            print(
                f"Chunk {index + 1} attempt {attempt + 1} failed: "
                f"{safe_error_summary(error)}"
            )
            if "429" in str(error) and attempt < 2:
                delay = retry_delay_seconds(error)
                print(f"Rate limited; retrying chunk {index + 1} in {delay:.1f}s...")
                time.sleep(delay)
                continue
            if attempt == 2:
                return index, None, 24000, error
    return index, None, 24000, RuntimeError("Maximum retries reached.")


def generate(
    chunks: list[str],
    voice: str,
    profile: str | None,
    scene: str | None,
    notes: str | None,
    context: str | None,
    output_path: Path,
    speed: float,
    max_workers: int,
    requests_per_minute: float,
    allow_partial: bool,
) -> None:
    if max_workers < 1:
        raise ValueError("max-workers must be at least 1.")
    if requests_per_minute < 0:
        raise ValueError("requests-per-minute must be zero or greater.")

    model = resolve_model()
    limiter = (
        RequestRateLimiter(requests_per_minute) if requests_per_minute else None
    )
    results: list[bytes | None] = [None] * len(chunks)
    sample_rates: list[int | None] = [None] * len(chunks)
    errors: list[tuple[int, Exception]] = []

    print(
        f"Generating {len(chunks)} chunk(s) with model {model}, voice {voice}, "
        f"max_workers={max_workers}, throttle={requests_per_minute:g} rpm..."
    )
    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = [
            executor.submit(
                process_chunk,
                index,
                chunk,
                len(chunks),
                voice,
                profile,
                scene,
                notes,
                context,
                model,
                resolve_api_key(),
                limiter,
            )
            for index, chunk in enumerate(chunks)
        ]
        for future in concurrent.futures.as_completed(futures):
            index, pcm, sample_rate, error = future.result()
            if error:
                errors.append((index, error))
            else:
                results[index] = pcm
                sample_rates[index] = sample_rate

    if errors and not allow_partial:
        failed = ", ".join(str(index + 1) for index, _ in sorted(errors))
        raise RuntimeError(f"Audio generation failed for chunk(s) {failed}; no output written.")

    available_rates = {rate for rate in sample_rates if rate is not None}
    if len(available_rates) > 1:
        raise RuntimeError(f"Chunks returned inconsistent sample rates: {sorted(available_rates)}")
    sample_rate = next(iter(available_rates), 24000)
    combined = b"".join(result for result in results if result)
    if not combined:
        raise RuntimeError("No audio data generated.")

    output_path = output_path.expanduser().resolve()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix=".pcm", delete=False) as temporary:
        temporary.write(combined)
        pcm_path = Path(temporary.name)

    command = [
        "ffmpeg",
        "-y",
        "-f",
        "s16le",
        "-ar",
        str(sample_rate),
        "-ac",
        "1",
        "-i",
        str(pcm_path),
    ]
    if speed != 1.0:
        command.extend(["-af", f"atempo={speed}"])
    command.append(str(output_path))

    try:
        subprocess.run(command, check=True, capture_output=True)
    except FileNotFoundError as error:
        raise RuntimeError("ffmpeg is required but was not found on PATH.") from error
    except subprocess.CalledProcessError as error:
        message = error.stderr.decode(errors="replace").strip()
        raise RuntimeError(f"ffmpeg conversion failed: {message}") from error
    finally:
        pcm_path.unlink(missing_ok=True)

    if not output_path.exists() or output_path.stat().st_size == 0:
        raise RuntimeError(f"MP3 output was not created: {output_path}")
    print(f"Audio saved: {output_path}")


def play_audio(path: Path) -> None:
    players = [
        ("afplay", ["afplay", str(path)]),
        ("ffplay", ["ffplay", "-nodisp", "-autoexit", str(path)]),
        ("mpv", ["mpv", "--no-video", str(path)]),
    ]
    for executable, command in players:
        if shutil.which(executable):
            subprocess.run(command, check=False)
            return
    print("Warning: playback requested, but afplay, ffplay, and mpv are unavailable.")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Generate MP3 audio with Gemini TTS")
    source = parser.add_mutually_exclusive_group()
    source.add_argument("--text", help="Text to synthesize")
    source.add_argument("--file", type=Path, help="UTF-8 text or Markdown file")
    parser.add_argument("--voice", help="Prebuilt voice name")
    parser.add_argument("--profile", help="Speaker profile or archetype")
    parser.add_argument("--scene", help="Recording scene")
    parser.add_argument("--notes", help="Delivery, pacing, and accent notes")
    parser.add_argument("--context", help="Optional story or delivery context")
    parser.add_argument("--output", type=Path, default=Path("output.mp3"))
    parser.add_argument("--speed", type=float, help="Playback speed from 0.5 to 2.0")
    parser.add_argument("--max-workers", type=int)
    parser.add_argument("--requests-per-minute", type=float)
    parser.add_argument("--allow-partial", action="store_true")
    parser.add_argument("--play", action="store_true")
    parser.add_argument("--dry-run", action="store_true", help="Inspect resolved settings without calling Gemini")
    parser.add_argument("--template", help="Template identifier from templates.json")
    parser.add_argument("--list-templates", action="store_true")
    parser.add_argument("--list-voices", action="store_true")
    parser.add_argument("--show-template")
    return parser


def main() -> None:
    args = build_parser().parse_args()
    catalog = load_catalog()
    if args.list_voices:
        list_voices(catalog)
        return
    if args.list_templates:
        list_templates(catalog)
        return
    if args.show_template:
        show_template(catalog, args.show_template)
        return
    if not args.text and not args.file:
        raise ValueError("Provide --text or --file.")

    text = args.file.read_text(encoding="utf-8") if args.file else args.text
    voice, profile, scene, notes, speed = resolve_args(args, catalog)
    chunks = chunk_text(text)
    max_workers = args.max_workers
    if max_workers is None:
        max_workers = int(os.environ.get("GEMINI_TTS_MAX_WORKERS", "1"))
    requests_per_minute = args.requests_per_minute
    if requests_per_minute is None:
        requests_per_minute = float(os.environ.get("GEMINI_TTS_RPM", "8"))

    if args.dry_run:
        print(f"model={resolve_model()}")
        print(f"voice={voice}")
        print(f"speed={speed:g}")
        print(f"chunks={len(chunks)}")
        print(f"output={args.output.expanduser().resolve()}")
        return

    generate(
        chunks,
        voice,
        profile,
        scene,
        notes,
        args.context,
        args.output,
        speed,
        max_workers,
        requests_per_minute,
        args.allow_partial,
    )
    if args.play:
        play_audio(args.output.expanduser().resolve())


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        raise SystemExit(f"Error: {error}") from error
