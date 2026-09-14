---
name: readback
description: Restate the user's intention to verify alignment.
metadata:
  category: writing-style
---

## Definition

A readback repeats an instruction, request, or piece of information back to the user. It states what the agent believes the user asked or meant, so the user can confirm or correct the understanding before work continues.

Use it to catch misunderstandings before they become wrong work.

## Purpose

- Confirm that the agent correctly received and understood the user's instruction or intent.
- Allow the user to verify accuracy and correct any misunderstanding.
- Prevent errors caused by acting on a mistaken interpretation.

## Process

1. Inspect the relevant context.
2. Infer the user's intention or meaning.
3. Keep important constraints and conditions.
4. Name assumptions or unresolved ambiguity.
5. Give the readback.
6. Stop and wait for confirmation or correction.

## Domain Language + Plain-English Gloss

Use established **terms of art** when they accurately describe the user's intent, but immediately gloss them in plain English.

Prefer:

> Use a **vertical slice** — build one small part of the feature end-to-end so it can actually be tested.

Over either:

> Use a vertical slice.

or:

> Build one small part through every layer of the system...

The idea is to preserve useful domain language without requiring the user to already know it.

Do not introduce jargon just to sound technical. Use it when it gives the idea a useful name.

## Rules

- Do not turn the readback into an implementation plan.
- Do not silently resolve meaningful ambiguity.
- Preserve conditions closely enough that their meaning cannot be lost.
- Use domain language when useful, followed by a plain-English gloss.
- If the user corrects the readback, update it before proceeding.

## Principle

Make sure your response sounds like a person talking to another.
Drop jargon, ceremony, repetition, and unnecessary structure. Preserve the meaning and try to match user's tone/voice.
