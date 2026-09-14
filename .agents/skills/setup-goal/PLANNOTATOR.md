# Optional Plannotator Adapter

Use this adapter only when `plannotator` is already available. It accelerates setup but does not change the canonical goal package.

## Session rule

Plannotator opens a user-driven browser session. Run each command in the foreground and wait until the user submits, dismisses, or explicitly asks to stop. Do not close, kill, refresh, restart, or open another session merely because the command appears idle.

## Intent interview

Use the interview UI when several material decisions remain. Write `interview.json` beside the goal package:

```json
{
  "stage": "interview",
  "title": "Short human-readable title",
  "goalSlug": "<slug>",
  "questions": [
    {
      "id": "scope",
      "prompt": "What should be in scope?",
      "description": "Why this decision changes the goal.",
      "answerMode": "multi-custom",
      "recommendedAnswer": "Recommended scope and rationale.",
      "recommendedOptionIds": ["ui"],
      "options": [
        { "id": "ui", "label": "UI" },
        { "id": "server", "label": "Server" }
      ],
      "required": true
    }
  ]
}
```

Supported answer modes are `text`, `single`, `multi`, `custom`, `single-custom`, and `multi-custom`.

Run:

```bash
plannotator setup-goal interview ai-artifacts/goals/<slug>/interview.json --json > ai-artifacts/goals/<slug>/interview-result.json
```

Read every answer and note. If the user expresses uncertainty, asks a question, or skips a blocking item, address it before writing `intent.md`. Tool JSON is provenance; synthesize the accepted result into `intent.md`.

## Fact review

Write `facts-review.json`:

```json
{
  "stage": "facts",
  "title": "Short human-readable title",
  "goalSlug": "<slug>",
  "facts": [
    {
      "id": "fact-1",
      "text": "The proposed fact text.",
      "accepted": false,
      "removed": false,
      "recommendedAutomatedVerification": true,
      "automatedVerification": true
    }
  ]
}
```

Run:

```bash
plannotator setup-goal facts ai-artifacts/goals/<slug>/facts-review.json --json > ai-artifacts/goals/<slug>/facts-result.json
```

Apply accepted, edited, and removed facts directly to `facts.md` and `facts.meta.json`. Preserve comments and verification selections. If revising a previous review, carry accepted facts and stable IDs forward rather than recreating them from memory.

## Plan gate

Review the completed plan with:

```bash
plannotator annotate ai-artifacts/goals/<slug>/plan.md --gate
```

If denied, revise from the returned annotations and run the gate again. Plannotator approval satisfies the explicit plan-approval requirement in the core skill.

## Dismissal and fallback

If a session is dismissed, stop that phase and tell the user. Continue through another available question or review tool only when the user asks to proceed without Plannotator.
