---
name: chatgpt-imagegen
description: Generates or edits raster images through OpenAI's Image API. Use for text-to-image work, reference-based edits, masks, transparent assets, and product or editorial visuals.
compatibility: Requires Python 3.10+, openai 2.16+, network access, and an OpenAI API key.
metadata:
  category: visual-media
---

# ChatGPT Image Generation

Use the bundled `chatgpt-img` CLI for generation and edits. Resolve its path relative to this `SKILL.md`.

## Setup

```bash
python3 -m pip install -r <skill-directory>/requirements.txt
export OPENAI_API_KEY='...'
```

`OPENCODE_OPENAI_API_KEY` is accepted as a fallback. `CHATGPT_IMAGE_DEFAULT` overrides the default model.

Image requests cost money. Confirm scope before generating many images or using high quality repeatedly.

## Generate

```bash
<skill-directory>/scripts/chatgpt-img \
  'A linocut illustration of a lighthouse in heavy rain' \
  ./lighthouse.png \
  --size 1536x1024 \
  --quality high
```

## Edit

Single reference:

```bash
<skill-directory>/scripts/chatgpt-img edit \
  ./portrait.png \
  'Use warm editorial lighting; preserve identity, pose, and clothing' \
  ./portrait-edited.png \
  --quality high
```

Multiple references:

```bash
<skill-directory>/scripts/chatgpt-img \
  --input ./portrait.png \
  --input ./moodboard.png \
  'Apply the moodboard lighting and palette while preserving the portrait identity' \
  ./portrait-styled.png
```

Optional masked edit:

```bash
<skill-directory>/scripts/chatgpt-img edit \
  ./input.png \
  'Replace the masked background with a cloudy coastline' \
  ./output.png \
  --mask ./mask.png
```

## Important options

- `--model`: defaults to `gpt-image-2`
- `--size`: supported dimensions or `auto`
- `--quality`: GPT Image uses `auto`, `low`, `medium`, or `high`
- `--background`: `auto`, `transparent`, or `opaque`
- `--format`: `png`, `jpeg`, or `webp`
- `--compression`: `0` through `100` for JPEG/WebP
- `--num`: number of outputs
- `--input`: repeatable reference image for edit mode
- `--mask`: optional edit mask
- `--input-fidelity`: supported by selected older GPT Image models, not `gpt-image-2`

Use `--help` and `edit --help` for the complete interface.

## Prompting edits

State both the change and the invariants. For example, name the identity, geometry, text, logos, pose, camera angle, or materials that must remain unchanged. Describe lighting, palette, composition, and finish positively instead of relying on long negative lists.

## Verification

1. Check the process exit status.
2. Confirm every expected output file exists and is non-empty.
3. Report exact paths and any revised prompt returned by the API.
4. Inspect the image with an available viewer when visual verification is part of the task.

Never expose API keys. Error output may include safe request metadata, but it must not include prompt contents or credentials.
