---
name: goal
description: Executes an approved goal package from ai-artifacts/goals. Use when the user provides a goal.md path or asks to implement a goal created by setup-goal.
metadata:
  category: planning-architecture
---

# Execute Goal

Execute an approved package at `ai-artifacts/goals/<slug>/goal.md`. Invoking this skill with a package is approval to begin implementation; it is not permission to rediscover scope or rewrite the contract.

## Load the contract

1. Read repository instructions first.
2. Require an explicit `goal.md` path from the user. Do not guess the latest or nearest package.
3. Read `goal.md` and resolve its links relative to the package directory.
4. Require and read `facts.md`, `facts.meta.json`, `plan.md`, and `dev-log.md`.
5. Read `intent.md` when the package contains or links it. Older approved packages without `intent.md` remain valid.
6. Stop if a required file is missing, a link is broken, fact metadata does not match the accepted facts, or a product, scope, architecture, security, or destructive decision remains unresolved.

Accepted facts are the implementation contract. Do not edit `intent.md` when present, `facts.md`, or `facts.meta.json` during execution. Do not revise `plan.md` unless the user explicitly sends the package back to setup or replanning.

## Execute

1. Briefly state the goal, approved scope, and execution approach.
2. Follow the approved plan in order. Use the narrowest implementation that satisfies every accepted fact.
3. Preserve unrelated work. Keep one writer per worktree unless parallel work is deliberately isolated.
4. Append concise entries to `dev-log.md`: completed work, changed files, validation, blockers, and remaining work. Never rewrite prior entries or claim work not performed.
5. Run every verification required by `plan.md`.
6. For each fact whose `automatedVerification` is `true`, run or add a concrete automated check. Record a blocker instead of pretending a check passed.
7. Record manual evidence for facts that cannot be verified automatically.

If implementation exposes a new material decision or contradicts an accepted fact, stop and ask the user. Do not silently expand scope or mutate the contract.

## Delegation

Delegate only when the host supports it and the role is directly useful. Every delegated task must name the exact `goal.md` path and instruct the worker to load the package. Never ask another agent to infer which goal applies.

Use isolated worktrees for parallel writers. Read-only research and review may run in parallel without sharing write ownership.

## Finish

Before declaring completion, compare the implementation and evidence against every accepted fact and the done condition in `goal.md`.

Report:

- implemented behavior;
- changed files;
- checks run and outcomes;
- manual verification;
- blockers or remaining work;
- `dev-log.md` path.
