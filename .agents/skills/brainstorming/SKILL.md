---
name: brainstorming
description: Explores and validates a feature, component, workflow, or behavior change before implementation. Use when work needs clarified intent, compared approaches, and an approved direction.
metadata:
  category: planning-architecture
---

# Brainstorming

Use collaborative design before creative implementation. Understand the problem, expose meaningful choices, and agree on a direction before writing code.

Do not force this workflow onto trivial, mechanical, or already-specified work.

## 1. Understand the context

Inspect repository instructions, relevant files, existing patterns, prior decisions, and nearby behavior before proposing a design. Ask the user only for judgments the repository cannot answer.

Restate the intended outcome in a few sentences. Correct misunderstandings before exploring solutions.

## 2. Resolve discovery depth

Use the lightest available question tool for one quick decision. When the idea needs formal scope, several material decisions, testable facts, and an execution plan, hand it to the `setup-goal` skill rather than recreating that workflow here.

Brainstorming explores design direction. `setup-goal` turns an agreed direction into an implementation contract.

## 3. Explore approaches

Propose two or three meaningfully different approaches. For each, explain:

- the core idea;
- what it optimizes for;
- important trade-offs;
- fit with existing project conventions;
- what would make it the wrong choice.

Recommend one approach and explain why. Avoid fake alternatives that differ only cosmetically.

## 4. Present the design incrementally

Present the proposed design in small, coherent sections and ask whether each section is correct before continuing. Cover only what the work needs, such as:

- user or operator flow;
- components and responsibilities;
- data and state transitions;
- boundaries and integrations;
- failure behavior;
- testing and verification.

Return to unresolved sections when feedback changes an earlier assumption.

## 5. Capture the approved design

Write durable design Markdown under:

```text
ai-artifacts/plans/YYYY-MM-DD-<topic>-design.md
```

Keep related sketches and diagrams embedded in that Markdown as ASCII or Mermaid. Do not create standalone diagram files unless the user explicitly requests another format.

Include:

- problem and intended outcome;
- chosen approach;
- rejected alternatives worth remembering;
- design details;
- constraints and assumptions;
- unresolved questions, if any;
- verification strategy.

Use an available document-review tool for final feedback. Prefer Plannotator annotation when available; otherwise ask for explicit approval through the host's question tool or chat.

## 6. Handoff

After approval, ask whether the user wants to formalize the work with `setup-goal`. Do not begin implementation without explicit authorization.
