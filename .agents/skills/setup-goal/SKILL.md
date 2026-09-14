---
name: setup-goal
description: Turns an idea into an approved goal package under ai-artifacts. Use when work needs clarified intent, scope, accepted facts, an implementation plan, and an explicit handoff.
metadata:
  category: planning-architecture
---

# Setup Goal

Turn an idea into an execution-ready package at `ai-artifacts/goals/<slug>/`. Extract intent, resolve material uncertainty, agree on testable facts, and produce a concrete plan before implementation begins.

Do not implement the goal while running this skill.

## Goal package

```text
ai-artifacts/goals/<slug>/
├── intent.md
├── facts.md
├── facts.meta.json
├── plan.md
├── dev-log.md
└── goal.md
```

Create files only as their phase is reached. Markdown files are the human-readable source of truth. Tool-specific working files may live beside them but must not become required inputs for execution.

## Choose the interaction path

Inspect which question and review tools are available before asking the user anything.

- For several material decisions, prefer Plannotator when the `plannotator` CLI is available. Read [PLANNOTATOR.md](./PLANNOTATOR.md) and use its interview and review adapters.
- Otherwise, use the host's structured question or interview tool.
- If no question tool exists, present a short, answerable bundle in chat.
- For one quick decision, use the lightest available question tool instead of opening a full interview.

Never require or install Plannotator. The goal workflow must work without it.

## 1. Rearticulate the goal

State the intended outcome in two or three sentences. Include who benefits and what changes. If the request is vague, perform only enough repository exploration to ground the restatement.

Ask the user to correct the restatement before continuing. Do not ask them to repeat details already present in the conversation.

Create `ai-artifacts/goals/<slug>/` after the outcome and slug are clear.

## 2. Extract intent

Inspect repository instructions, relevant code, docs, tests, configuration, prior decisions, and similar implementations. Answer factual questions from evidence instead of asking the user.

Separate what you learn into:

- **Outcome:** the observable result the user wants.
- **Audience:** who uses or operates it.
- **Problem:** why the change matters.
- **Scope:** behavior included in this goal.
- **Non-goals:** nearby behavior intentionally excluded.
- **Constraints:** technical, product, security, time, compatibility, or policy limits.
- **Decisions:** choices only the user can make.
- **Assumptions:** low-risk defaults accepted provisionally.
- **Unknowns:** unresolved items that could materially change the work.

Ask only questions whose answers could change scope, user-visible behavior, architecture, data, permissions, safety, rollout, or success criteria. Each question should include context, meaningful options when possible, and a recommended answer.

Prefer fewer high-leverage questions over exhaustive questionnaires. Do not use a relentless one-question loop. Bundle related decisions when the available tool supports it.

Write `intent.md`:

```md
# Intent

## Outcome

## Audience and problem

## Scope

## Non-goals

## Constraints

## Decisions

## Assumptions

## Open questions
```

Do not leave a blocking question unresolved. The user may explicitly accept a named assumption instead.

## 3. Build the fact contract

Convert the approved intent into a flat list of concrete, testable facts. A fact describes observable behavior, a boundary, or a verifiable constraint. Keep implementation choices out unless they are themselves approved requirements.

Review the facts with the user. They must be able to accept, edit, or remove each fact. Do not proceed until the fact set is explicitly approved.

Write accepted facts to `facts.md`:

```md
# Facts

- The system ...
- A user can ...
- The change does not ...
```

Write matching metadata to `facts.meta.json`:

```json
[
  {
    "id": "fact-1",
    "text": "The accepted fact text.",
    "comment": "",
    "recommendedAutomatedVerification": true,
    "automatedVerification": true
  }
]
```

Use stable IDs. `recommendedAutomatedVerification` records the agent's recommendation; `automatedVerification` records the user's accepted choice. Set them to `true` when a concrete automated check should prove the fact. Preserve user comments that affect interpretation.

## 4. Produce the plan

Explore the implementation path for every accepted fact. Trace existing code and name exact files or systems where possible.

Write `plan.md` with:

- a brief solution approach;
- ordered, bounded implementation steps;
- files or systems touched by each step;
- concrete verification for each step;
- coverage for every fact marked `automatedVerification: true`;
- risks, accepted assumptions, and remaining non-blocking unknowns.

Review the plan with the user using the best available review tool. Prefer Plannotator annotation when available; otherwise request explicit approval through the host's question tool or chat. If rejected, revise and review again.

## 5. Finalize the package

Create `dev-log.md`:

```md
# Dev Log

Status: Not started
```

Do not fabricate progress. Execution appends work and evidence later.

Create `goal.md`:

```md
# Goal

{One to three sentences describing the approved outcome.}

## Contract

- [Intent](./intent.md)
- [Facts](./facts.md)
- [Fact metadata](./facts.meta.json)
- [Plan](./plan.md)
- [Dev log](./dev-log.md)

## Done when

{Concise completion condition derived from the accepted facts and plan.}
```

Before finishing, verify that every link resolves, every fact has matching metadata, the plan covers every fact, and no blocking decision remains.

Tell the user the package path and that it is ready for the `goal` execution skill. If the host exposes a `/goal` command, it may be launched as:

```text
/goal ai-artifacts/goals/<slug>/goal.md
```

## Revisions

When intent or facts change, return the package to setup rather than silently changing the execution contract. Update `intent.md`, review affected facts again, revise the plan, and preserve stable fact IDs where their meaning did not change.
