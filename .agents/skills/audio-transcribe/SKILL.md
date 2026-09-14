---
name: audio-transcribe
description: Transcribes local audio into Markdown with Gemini 3.5 Transcribe, including speaker labels and provider timestamps. Use for interviews, meetings, lectures, podcasts, voice notes, and similar recordings.
compatibility: Requires Bun 1.1+, ffmpeg/ffprobe, network access, and a Gemini API key.
metadata:
  category: visual-media
---

# Audio Transcription

The bundled CLI normalizes audio, uploads each chunk to Gemini 3.5 Transcribe, and writes one Markdown transcript. Default output uses Gemini's speaker diarization and word-level timestamps.

## Setup

Resolve paths relative to this `SKILL.md`. Do not assume a global skill directory.

```bash
bun install --cwd <skill-directory>/scripts --no-save
export GEMINI_API_KEY='...'
```

`GOOGLE_API_KEY` and `OPENCODE_GOOGLE_API_KEY` are fallbacks. Set `GEMINI_TRANSCRIBE_MODEL` only to select a compatible replacement model. The default is `gemini-3.5-transcribe`.

## Before transcription

1. Confirm the input exists and uses a supported format.
2. Use `ffprobe` to inspect duration.
3. Agree on an output path when the user does not provide one.
4. Warn before an unexpectedly long or costly transcription.
5. Ask for language hints and domain terms when they are known and likely to improve recognition.

Do not upload media without the user's explicit transcription request.

## Run

```bash
bun run <skill-directory>/scripts/transcribe.ts \
  ./interview.m4a \
  --language en-US \
  --vocabulary "SumUp" \
  --output ./interview-transcript.md
```

Options:

- `--chunk-minutes N`: chunk duration. The default is `30`.
- `--language CODE`: BCP-47 language hint. Repeat the option for each known language. Omit it for automatic language detection and code-switching.
- `--vocabulary TERM`: domain term, acronym, or proper name. Repeat the option for each term. Use it with `--smart`. Gemini accepts up to 1,000 terms. It normally performs best with a short, targeted list.
- `--smart`: return a cleaned transcript. This removes filler words, repetitions, and false starts, but Gemini does not provide speaker labels or timestamps in this mode.
- `--output PATH`: exact Markdown output path.
- `--keep`: retain normalized audio and chunk files beside the transcript.
- `--help`: show usage.

Default verbatim mode requests speaker diarization and word timestamps. Gemini limits those requests to 30 minutes of audio per request, so the CLI splits longer recordings. The live API rejects custom vocabulary with word timestamps, so the CLI fails clearly when `--vocabulary` is used without `--smart`. `--smart` supports chunks up to 60 minutes because it does not request either feature. If Gemini returns a rate-limit delay, the CLI waits for that delay and retries the interaction once.

Supported inputs: MP3, WAV, M4A, OGG, FLAC, and WebM.

## Output

Default output contains source metadata, duration, model, mode, language hints, custom vocabulary, chunk count, speaker-labelled turns, and timestamps from Gemini's word annotations. The CLI orders annotations by their audio timestamps before it formats turns. The CLI deletes each remote Gemini upload after its transcription request. It warns if that deletion fails.

`--smart` produces clean text from `interaction.output_text`. It does not invent timestamps or speaker identity that the API did not return.

Speaker labels identify distinct voices within one Gemini request, not verified people. The CLI adds a heading for each chunk because Gemini can reset labels after a split. Preserve labels such as `Speaker 1` when names are uncertain. Do not present inferred identities as facts.

## Verification

After transcription:

1. Confirm that the command succeeded.
2. Confirm that the Markdown file exists and is non-empty.
3. Inspect the beginning, one middle section, and the end for missing chunks, speaker changes, or malformed timestamps.
4. Report the exact path and disclose failed or partial processing.

The CLI does not write a transcript after a failed API request. It removes local normalized audio unless `--keep` is set. Never print API keys or add private media and generated transcripts to the skill repository.
