---
name: domain-modeling
description: Maintains project domain language, context maps, diagrams, and architectural decisions under ai-artifacts. Use when clarifying terminology, bounded contexts, or durable design decisions.
metadata:
  category: planning-architecture
---

# Domain Modeling

Actively build and sharpen the project's domain model as you design. This is the *active* discipline—challenging terms, inventing edge-case scenarios, visualizing relationships, and writing the glossary and decisions down when they crystallize. Merely reading `ai-artifacts/CONTEXT.md` for vocabulary is not this skill; this skill is for changing the model, not just consuming it.

## Artifact home

Keep agent-authored project knowledge under `ai-artifacts/`. This includes context glossaries, context maps, ADRs, plans, specs, analyses, reports, reviews, and other Markdown primarily created for or by an LLM.

Use another location only when the user or repository explicitly requires it, such as a human-facing `README.md` or established product documentation. Do not relocate existing documentation unless asked.

Embed ASCII or Mermaid diagrams directly in the Markdown they explain. Do not create standalone diagram files.

## File structure

Most repositories have a single context:

```text
/
├── ai-artifacts/
│   ├── CONTEXT.md
│   └── docs/
│       └── adr/
│           ├── 0001-event-sourced-orders.md
│           └── 0002-postgres-for-write-model.md
└── src/
```

If `ai-artifacts/CONTEXT-MAP.md` exists, the repository has multiple contexts:

```text
/
├── ai-artifacts/
│   ├── CONTEXT-MAP.md
│   ├── docs/
│   │   └── adr/                      ← system-wide decisions
│   └── src/
│       ├── ordering/
│       │   ├── CONTEXT.md
│       │   └── docs/adr/             ← context-specific decisions
│       └── billing/
│           ├── CONTEXT.md
│           └── docs/adr/
└── src/                              ← application source code
```

`ai-artifacts/src/` mirrors the source tree only to make context ownership obvious. Application code remains in the repository's real source directories.

Create files lazily—only when there is something worth recording. Create `ai-artifacts/CONTEXT.md` when the first term is resolved. Create an ADR directory when the first qualifying decision is made. Create `ai-artifacts/CONTEXT-MAP.md` only when the project has multiple genuine contexts.

## During the session

### Challenge against the glossary

When the user uses a term that conflicts with the relevant `CONTEXT.md`, call it out immediately. "Your glossary defines 'cancellation' as X, but you seem to mean Y—which is it?"

### Sharpen fuzzy language

When the user uses vague or overloaded terms, propose a precise canonical term. "You're saying 'account'—do you mean the Customer or the User? Those are different things."

### Discuss concrete scenarios

When domain relationships are being discussed, stress-test them with specific scenarios. Invent scenarios that probe edge cases and force the user to be precise about boundaries between concepts.

### Cross-reference with code

When the user states how something works, check whether the code agrees. If you find a contradiction, surface it: "Your code cancels entire Orders, but you just said partial cancellation is possible—which is right?"

### Visualize the model

Include an embedded diagram when boundaries, relationships, states, or flows are easier to understand visually.

- Use Mermaid for graphs, state transitions, sequences, and multi-context relationships.
- Use ASCII art for small structures or when plain-text portability matters more.
- Use domain terms from the glossary.
- Show the domain model, not classes, framework components, or incidental infrastructure.
- Keep the diagram beside the prose it clarifies.

### Update context artifacts inline

When a term or relationship is resolved, update the relevant file under `ai-artifacts/` immediately. Do not batch resolved domain knowledge. Use [CONTEXT-FORMAT.md](./CONTEXT-FORMAT.md).

A `CONTEXT.md` is a glossary, not a spec, scratch pad, or implementation guide. Its diagrams may show conceptual relationships, boundaries, and states, but must not contain implementation details.

### Offer ADRs sparingly

Only offer to create an ADR when all three are true:

1. **Hard to reverse**—the cost of changing the decision later is meaningful.
2. **Surprising without context**—a future reader will wonder why it was done this way.
3. **The result of a real trade-off**—genuine alternatives existed and one was chosen for specific reasons.

If any condition is missing, skip the ADR. Use [ADR-FORMAT.md](./ADR-FORMAT.md).
