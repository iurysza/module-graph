---
name: reply-bro
description: Rewrites technical review replies as short, natural Slack or PR messages between engineers. Use when asked to make a reply more casual, concise, less formal, or less AI-generated while keeping its main technical point.
metadata:
  category: writing-style
---

# Reply bro

Turn the supplied reply into a short message that sounds like an engineer typed it quickly in Slack or a PR discussion.

## Rules

- Output only the rewritten message. Do not add headings, bullets, or commentary.
- Use 3 or 4 short sentences.
- Start with a positive acknowledgement, such as “Good shout.”
- Keep only the main technical concern. Cut specification detail, repeated reasoning, and formal language.
- Raise the concern as a question, not a rejection. Use natural phrasing such as “Would this fit here though?” or “It looks like…”.
- End by asking whether a simpler alternative would work, for example: “Maybe keeping `existing.attribute` is simpler?”
- Use plain, casual language. Avoid sounding certain, argumentative, or like a formal technical review.

## Check before replying

Confirm the result starts positively, has 3–4 sentences, makes the concern a question, and ends with the simpler alternative.

## Example

**Source:**

> That page is why we should not use `hw.memory.size`. It is a hardware metric for a DIMM in a monitored host, not a span attribute for phone RAM. The specification defines it as an UpDownCounter representing the size of a memory module and requires hardware-specific identity attributes. A mobile launch span has none of that. Additionally, calculating the RAM bucket in Honeycomb would require sending and storing the raw RAM value. We should keep `sumup.device.ram_bucket`.

**Reply:**

> Good shout. Would `hw.memory.size` fit here though? It looks like a host memory-module metric, while we need total RAM on a mobile launch span. Calculating it in Honeycomb would also mean sending raw RAM. Maybe keeping `sumup.device.ram_bucket` is simpler?
