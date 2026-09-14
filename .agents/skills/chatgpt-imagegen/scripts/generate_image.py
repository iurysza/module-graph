#!/usr/bin/env python3
"""
Generate or edit images with OpenAI's Image API.

Examples:
    # Generate from text
    chatgpt-img "A cat wearing a wizard hat" output.png

    # Edit one input image with the latest GPT Image model
    chatgpt-img --input source.png "Make it a warm editorial portrait" output.png

    # Same edit flow with an explicit subcommand
    chatgpt-img edit source.png "Make it a warm editorial portrait" output.png

Environment:
    OPENCODE_OPENAI_API_KEY - Preferred API key
    OPENAI_API_KEY          - Fallback API key
    CHATGPT_IMAGE_DEFAULT   - Optional default model override
"""

from __future__ import annotations

import argparse
import base64
import os
import re
import sys
import urllib.request
from contextlib import ExitStack
from pathlib import Path
from typing import Any

DEFAULT_MODEL = "gpt-image-2"
MODEL_CHOICES = [
    "gpt-image-2",
    "gpt-image-1.5",
    "gpt-image-1",
    "gpt-image-1-mini",
    "dall-e-3",
    "dall-e-2",
]


class ImageRequestError(RuntimeError):
    """Raised when the OpenAI image API rejects a request."""

    def __init__(self, mode: str, kwargs: dict[str, Any], cause: Exception):
        self.mode = mode
        self.request_fields = safe_kwargs(kwargs)
        super().__init__(build_error_message(mode, kwargs, cause))


def api_key() -> str:
    key = os.environ.get("OPENAI_API_KEY") or os.environ.get("OPENCODE_OPENAI_API_KEY")
    if not key:
        raise EnvironmentError("No OpenAI API key found. Set OPENAI_API_KEY.")
    return key


def create_client() -> Any:
    try:
        from openai import OpenAI
    except ImportError as error:
        raise RuntimeError(
            "The openai package is required. Install requirements.txt first."
        ) from error
    return OpenAI(api_key=api_key())


def integer_between(minimum: int, maximum: int):
    def parse(value: str) -> int:
        parsed = int(value)
        if not minimum <= parsed <= maximum:
            raise argparse.ArgumentTypeError(
                f"must be between {minimum} and {maximum}"
            )
        return parsed

    return parse


def existing_file(path: str) -> Path:
    resolved = Path(path).expanduser().resolve()
    if not resolved.is_file():
        raise FileNotFoundError(f"Input file not found: {resolved}")
    return resolved


def resolve_model(model: str | None) -> str:
    return model or os.environ.get("CHATGPT_IMAGE_DEFAULT") or DEFAULT_MODEL


def is_dalle(model: str) -> bool:
    return model.startswith("dall-e")


def is_gpt_image_2(model: str) -> bool:
    return model == "gpt-image-2"


def validate_request_options(
    *,
    mode: str,
    model: str,
    size: str | None,
    quality: str | None,
    background: str | None,
    num_images: int,
    output_format: str,
    output_compression: int | None,
    moderation: str | None = None,
) -> None:
    if not 1 <= num_images <= 10:
        raise ValueError("Number of images must be between 1 and 10.")

    if model.startswith("gpt-image"):
        valid_sizes = {"auto", "1024x1024", "1536x1024", "1024x1536"}
        if size and size not in valid_sizes:
            raise ValueError(
                f"{model} {mode} size must be one of: {', '.join(sorted(valid_sizes))}."
            )
        if quality and quality not in {"auto", "low", "medium", "high"}:
            raise ValueError(
                f"{model} quality must be auto, low, medium, or high."
            )
        if output_compression is not None and output_format not in {"jpeg", "webp"}:
            raise ValueError("--compression requires --format jpeg or webp.")
        if background == "transparent" and output_format not in {"png", "webp"}:
            raise ValueError("Transparent backgrounds require PNG or WebP output.")
        return

    if mode == "edit":
        raise ValueError("Image edit mode requires a GPT Image model.")
    if background is not None:
        raise ValueError(f"{model} does not support --background.")
    if output_compression is not None:
        raise ValueError(f"{model} does not support --compression.")
    if moderation is not None:
        raise ValueError(f"{model} does not support --moderation.")

    if model == "dall-e-3":
        if num_images != 1:
            raise ValueError("dall-e-3 requires --num 1.")
        valid_sizes = {"1024x1024", "1024x1792", "1792x1024"}
        if size and size not in valid_sizes:
            raise ValueError(
                f"dall-e-3 size must be one of: {', '.join(sorted(valid_sizes))}."
            )
        if quality and quality not in {"standard", "hd"}:
            raise ValueError("dall-e-3 quality must be standard or hd.")
    elif model == "dall-e-2":
        valid_sizes = {"256x256", "512x512", "1024x1024"}
        if size and size not in valid_sizes:
            raise ValueError(
                f"dall-e-2 size must be one of: {', '.join(sorted(valid_sizes))}."
            )
        if quality:
            raise ValueError("dall-e-2 does not support --quality.")


def ensure_output_parent(output_path: str) -> None:
    parent = Path(output_path).expanduser().resolve().parent
    parent.mkdir(parents=True, exist_ok=True)


def add_common_args(
    parser: argparse.ArgumentParser,
    *,
    include_input: bool,
    include_moderation: bool = True,
) -> None:
    parser.add_argument(
        "--model",
        "-m",
        choices=MODEL_CHOICES,
        default=None,
        help=f"Model to use (default: {DEFAULT_MODEL}, or CHATGPT_IMAGE_DEFAULT)",
    )
    parser.add_argument(
        "--size",
        "-s",
        help="Image dimensions. Valid values depend on the selected model; use the skill documentation for model-specific sets.",
    )
    parser.add_argument(
        "--quality",
        "-q",
        choices=["auto", "low", "medium", "high", "standard", "hd"],
        help="Image quality (auto/low/medium/high for GPT Image; standard/hd for DALL-E 3)",
    )
    parser.add_argument(
        "--background",
        "-b",
        choices=["auto", "transparent", "opaque"],
        help="Background type (GPT Image only)",
    )
    parser.add_argument(
        "--num",
        "-n",
        type=integer_between(1, 10),
        default=1,
        help="Number of images to create, 1-10 (default: 1)",
    )
    parser.add_argument(
        "--format",
        "-f",
        choices=["png", "jpeg", "webp"],
        default="png",
        help="Output format (default: png)",
    )
    parser.add_argument(
        "--compression",
        "-c",
        type=integer_between(0, 100),
        help="Compression level 0-100 (jpeg/webp only)",
    )
    if include_moderation:
        parser.add_argument(
            "--moderation",
            choices=["auto", "low"],
            help="Moderation level (generation only; GPT Image only)",
        )
    if include_input:
        parser.add_argument(
            "--input",
            "-i",
            dest="inputs",
            action="append",
            help="Input image path for edit mode. Repeat for multiple reference images.",
        )
        parser.add_argument(
            "--mask",
            help="Optional mask image for image edits.",
        )
        parser.add_argument(
            "--input-fidelity",
            choices=["high", "low"],
            help="Edit fidelity hint for models that support it. gpt-image-2 does not, so this CLI omits it there.",
        )


def safe_kwargs(kwargs: dict[str, Any]) -> dict[str, Any]:
    safe: dict[str, Any] = {}
    for key, value in kwargs.items():
        if key == "prompt":
            safe[key] = f"<{len(str(value))} chars>"
        elif key == "image":
            safe[key] = "<input file(s)>"
        elif key == "mask":
            safe[key] = "<mask file>"
        else:
            safe[key] = value
    return safe


def safe_error_summary(cause: Exception) -> str:
    parts = [cause.__class__.__name__]
    status = getattr(cause, "status_code", None)
    if isinstance(status, int):
        parts.append(f"status={status}")
    code = getattr(cause, "code", None)
    if isinstance(code, str) and re.fullmatch(r"[A-Za-z0-9_.-]{1,64}", code):
        parts.append(f"code={code}")
    return " ".join(parts)


def build_error_message(mode: str, kwargs: dict[str, Any], cause: Exception) -> str:
    model = kwargs.get("model", "unknown")
    endpoint = "client.images.edit" if mode == "edit" else "client.images.generate"
    lines = [
        f"OpenAI image {mode} failed.",
        f"endpoint: {endpoint}",
        f"model: {model}",
        f"request fields: {safe_kwargs(kwargs)}",
        f"provider error: {safe_error_summary(cause)}",
    ]

    message = str(cause)
    hints: list[str] = []
    if "input_fidelity" in message or "input fidelity" in message.lower():
        hints.append("gpt-image-2 edit does not support --input-fidelity; omit it or use gpt-image-1.5.")
    if "response_format" in message:
        hints.append("Do not send response_format for GPT Image edits; this CLI intentionally avoids it.")
    if "size" in message.lower() and mode == "edit":
        hints.append("For edits, use --size auto, 1024x1024, 1536x1024, or 1024x1536.")
    if "quality" in message.lower():
        hints.append("For GPT Image use --quality auto/low/medium/high; DALL-E uses standard/hd.")
    if "api key" in message.lower():
        hints.append("Set OPENAI_API_KEY.")

    if hints:
        lines.append("hints:")
        lines.extend(f"- {hint}" for hint in hints)
    return "\n".join(lines)


def call_image_api(client: Any, mode: str, kwargs: dict[str, Any]):
    try:
        if mode == "edit":
            return client.images.edit(**kwargs)
        return client.images.generate(**kwargs)
    except Exception as exc:  # OpenAI SDK error types move around; keep CLI useful.
        raise ImageRequestError(mode, kwargs, exc) from exc


def save_response_images(response: Any, output_path: str) -> str | None:
    output_path = str(Path(output_path).expanduser())
    ensure_output_parent(output_path)
    revised_prompt = None
    data = getattr(response, "data", None) or []
    if not data:
        raise RuntimeError("No image data returned from API")

    for index, image_data in enumerate(data):
        path = output_path
        if len(data) > 1:
            base, ext = os.path.splitext(output_path)
            path = f"{base}-{index + 1}{ext}"

        b64_json = getattr(image_data, "b64_json", None)
        url = getattr(image_data, "url", None)
        if b64_json:
            image_bytes = base64.b64decode(b64_json)
        elif url:
            with urllib.request.urlopen(url, timeout=120) as response_file:
                image_bytes = response_file.read()
        else:
            raise RuntimeError("No b64_json or url returned from API")

        with open(path, "wb") as file:
            file.write(image_bytes)

        revised = getattr(image_data, "revised_prompt", None)
        if revised:
            revised_prompt = revised

    return revised_prompt


def generate_image(
    *,
    prompt: str,
    output_path: str,
    model: str | None = None,
    size: str | None = None,
    quality: str | None = None,
    background: str | None = None,
    num_images: int = 1,
    output_format: str = "png",
    output_compression: int | None = None,
    moderation: str | None = None,
) -> str | None:
    model = resolve_model(model)
    validate_request_options(
        mode="generate",
        model=model,
        size=size,
        quality=quality,
        background=background,
        num_images=num_images,
        output_format=output_format,
        output_compression=output_compression,
        moderation=moderation,
    )
    client = create_client()

    kwargs: dict[str, Any] = {
        "model": model,
        "prompt": prompt,
        "n": num_images,
    }
    if size:
        kwargs["size"] = size
    if quality:
        kwargs["quality"] = quality
    if background and not is_dalle(model):
        kwargs["background"] = background
    if output_format and not is_dalle(model):
        kwargs["output_format"] = output_format
    if output_compression is not None and not is_dalle(model):
        kwargs["output_compression"] = output_compression
    if moderation and not is_dalle(model):
        kwargs["moderation"] = moderation

    response = call_image_api(client, "generate", kwargs)
    return save_response_images(response, output_path)


def edit_image(
    *,
    prompt: str,
    input_paths: list[str],
    output_path: str,
    model: str | None = None,
    mask_path: str | None = None,
    size: str | None = None,
    quality: str | None = None,
    background: str | None = None,
    num_images: int = 1,
    output_format: str = "png",
    output_compression: int | None = None,
    input_fidelity: str | None = None,
) -> str | None:
    if not input_paths:
        raise ValueError("Edit mode requires at least one --input image or: chatgpt-img edit INPUT PROMPT OUTPUT")

    model = resolve_model(model)
    validate_request_options(
        mode="edit",
        model=model,
        size=size,
        quality=quality,
        background=background,
        num_images=num_images,
        output_format=output_format,
        output_compression=output_compression,
    )

    client = create_client()
    resolved_inputs = [existing_file(path) for path in input_paths]
    resolved_mask = existing_file(mask_path) if mask_path else None
    with ExitStack() as stack:
        image_files = [stack.enter_context(path.open("rb")) for path in resolved_inputs]
        image_arg: Any = image_files[0] if len(image_files) == 1 else image_files

        kwargs: dict[str, Any] = {
            "model": model,
            "image": image_arg,
            "prompt": prompt,
            "n": num_images,
        }
        if resolved_mask:
            kwargs["mask"] = stack.enter_context(resolved_mask.open("rb"))
        if size:
            kwargs["size"] = size
        if quality:
            kwargs["quality"] = quality
        if background:
            kwargs["background"] = background
        if output_format:
            kwargs["output_format"] = output_format
        if output_compression is not None:
            kwargs["output_compression"] = output_compression
        if input_fidelity and is_gpt_image_2(model):
            print("Warning: gpt-image-2 does not support --input-fidelity; omitting it.", file=sys.stderr)
        elif input_fidelity:
            kwargs["input_fidelity"] = input_fidelity

        response = call_image_api(client, "edit", kwargs)

    return save_response_images(response, output_path)


def generate_parser(program: str) -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog=program,
        description="Generate or edit images with OpenAI's Image API",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("prompt", help="Text prompt describing the image")
    parser.add_argument("output", help="Output file path (e.g., output.png)")
    add_common_args(parser, include_input=True)
    return parser


def edit_parser(program: str) -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog=program,
        description="Edit an input image with OpenAI's Image API",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("input", help="Input image path")
    parser.add_argument("prompt", help="Edit instruction")
    parser.add_argument("output", help="Output image path")
    add_common_args(parser, include_input=False, include_moderation=False)
    parser.add_argument("--mask", help="Optional mask image for image edits")
    parser.add_argument(
        "--input-fidelity",
        choices=["high", "low"],
        help="Edit fidelity hint for models that support it. gpt-image-2 does not, so this CLI omits it there.",
    )
    return parser


def print_success(output_path: str, num_images: int, revised_prompt: str | None) -> None:
    if num_images == 1:
        print(f"Image saved to: {output_path}")
    else:
        base, ext = os.path.splitext(output_path)
        print(f"Images saved to: {base}-1{ext} ... {base}-{num_images}{ext}")
    if revised_prompt:
        print(f"Revised prompt: {revised_prompt}")


def run_generate(args: argparse.Namespace) -> None:
    if args.inputs:
        revised = edit_image(
            prompt=args.prompt,
            input_paths=args.inputs,
            output_path=args.output,
            model=args.model,
            mask_path=args.mask,
            size=args.size,
            quality=args.quality,
            background=args.background,
            num_images=args.num,
            output_format=args.format,
            output_compression=args.compression,
            input_fidelity=args.input_fidelity,
        )
    else:
        revised = generate_image(
            prompt=args.prompt,
            output_path=args.output,
            model=args.model,
            size=args.size,
            quality=args.quality,
            background=args.background,
            num_images=args.num,
            output_format=args.format,
            output_compression=args.compression,
            moderation=args.moderation,
        )
    print_success(args.output, args.num, revised)


def run_edit(args: argparse.Namespace) -> None:
    revised = edit_image(
        prompt=args.prompt,
        input_paths=[args.input],
        output_path=args.output,
        model=args.model,
        mask_path=args.mask,
        size=args.size,
        quality=args.quality,
        background=args.background,
        num_images=args.num,
        output_format=args.format,
        output_compression=args.compression,
        input_fidelity=args.input_fidelity,
    )
    print_success(args.output, args.num, revised)


def main() -> None:
    program = "chatgpt-img"
    try:
        if len(sys.argv) > 1 and sys.argv[1] == "edit":
            parser = edit_parser(f"{program} edit")
            args = parser.parse_args(sys.argv[2:])
            run_edit(args)
        elif len(sys.argv) > 1 and sys.argv[1] == "generate":
            parser = generate_parser(f"{program} generate")
            args = parser.parse_args(sys.argv[2:])
            run_generate(args)
        else:
            parser = generate_parser(program)
            args = parser.parse_args()
            run_generate(args)
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
