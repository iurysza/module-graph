# ADR Format

System-wide ADRs live in:

```text
ai-artifacts/docs/adr/
```

Context-specific ADRs live beside their context artifact:

```text
ai-artifacts/src/<context>/docs/adr/
```

Use sequential names such as `0001-event-sourced-orders.md`. Create the target directory only when the first qualifying ADR is needed.

## Template

```md
# {Short title of the decision}

{One to three sentences: what was the context, what was decided, and why.}
```

That is enough for most decisions. The value is recording that a decision was made and why, not filling out ceremonial sections.

## Embedded diagrams

When a decision depends on relationships, boundaries, states, sequences, or data flow, embed a small Mermaid or ASCII diagram in the ADR. Keep it beside the explanation it clarifies. Do not create standalone diagram files.

A diagram should explain the decision, not decorate the document. Omit it when prose is clearer.

## Optional sections

Include these only when they add genuine value:

- **Status** frontmatter (`proposed | accepted | deprecated | superseded by ADR-NNNN`) when decisions may be revisited.
- **Considered options** when rejected alternatives are worth remembering.
- **Consequences** when downstream effects are not obvious.

## Numbering

Scan the target ADR directory for the highest existing number and increment it. System-wide and context-specific directories each maintain their own sequence.

## When to offer an ADR

All three conditions must be true:

1. **Hard to reverse**—changing the decision later has meaningful cost.
2. **Surprising without context**—a future reader will wonder why it was done this way.
3. **A real trade-off**—genuine alternatives existed and one was chosen for specific reasons.

If a decision is easy to reverse, obvious, or had no meaningful alternative, do not create an ADR.

### Decisions that often qualify

- Architectural shape.
- Integration patterns between contexts.
- Technology choices with meaningful lock-in.
- Boundary and ownership decisions.
- Deliberate deviations from the obvious path.
- Constraints not visible in code.
- Rejected alternatives whose rejection is non-obvious.
