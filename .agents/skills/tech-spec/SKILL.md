---
name: tech-spec
description: Produces implementation-ready technical specifications with concrete contracts, module boundaries, data flows, file changes, and test slices. Use for architecture or implementation handoffs.
metadata:
  category: planning-architecture
---

# Technical Specification

A tech spec is a typed architecture handoff. Types and call stacks define what changes; prose explains why.

This workflow is design-only. Do not implement. Write durable specs to `ai-artifacts/specs/YYYY-MM-DD-<topic>.md` unless the user or repository explicitly requires another location.

## Choose the path

- **Convert context:** use when conversation, docs, and code already define the problem and constraints.
- **Set up the goal first:** use the `setup-goal` skill when important requirements, boundaries, or acceptance criteria remain unresolved.

Answer repository questions by inspecting the repository. Ask the user only for decisions.

## Build the spec

### 1. Establish current state

Read relevant code, tests, docs, project instructions, domain vocabulary, and architectural decisions. Trace the existing entrypoint-to-effect flow.

Capture the problem, callers, goals, non-goals, constraints, invariants, affected systems, operational concerns, risks, and open questions. Do not invent missing requirements.

### 2. Compare real alternatives

Offer materially different designs. They should differ in ownership, interfaces, seam placement, call stack, runtime topology, or state model—not merely names.

For each option show:

- domain and application types
- public interfaces
- module and adapter ownership
- entrypoint-to-effect call stack
- failure and cancellation behavior
- persistence and transaction boundaries
- testing seams
- costs and risks

Choose the recommendation only after comparison.

### 3. Define contracts

Sketch every new or changed boundary in code:

```ts
type Input = { /* parsed application values */ };
type Output = { /* caller-visible result */ };
type ExpectedFailure = FailureA | FailureB;

interface Capability {
  execute(input: Input): Promise<Result<Output, ExpectedFailure>>;
}
```

Cover domain values, state variants, request/response shapes, parsers, functions, ports, adapters, records, events, and public APIs as applicable. State what may cross each boundary and what must remain private.

### 4. Trace behavior end to end

For every affected behavior, show the call and data flow:

```text
raw input
  -> protocol parser
  -> application/domain input
  -> service or domain operation
  -> outbound port
  -> adapter and external effect
  -> typed result or failure
  -> protocol projection
```

Include old versus proposed flow when changing an existing path. Add authorization, retries, cancellation, idempotency, transactions, observability, and runtime hops when reachable.

### 5. Map files

List files to add, change, and delete. Give each file one clear responsibility tied to a contract or call-stack step.

### 6. Plan vertical test slices

Use red-green cycles, one behavior at a time:

1. agree on the public seam;
2. add one failing behavior test;
3. write the minimum implementation to pass;
4. repeat with the next behavior.

Cover happy paths, important failures, parser rejection, domain invariants, adapter contracts, persistence/runtime semantics, and end-to-end high-consequence flows. Avoid a horizontal “all tests, then all code” plan.

## Required outline

```markdown
# Title

## Summary
## Current State
## Goals
## Non-Goals
## Invariants and Constraints
## Alternatives
## Recommendation
## Domain Model and Types
## Interfaces and APIs
## Boundaries and Adapters
## Call Stacks and Data Flow
## Files to Add, Change, or Delete
## Red-Green Test Plan
## Risks and Open Questions
```

Compress sections that do not apply, but never omit changed contracts, call stacks, seams, or tests merely because they are difficult to specify.

## Completion criteria

- every requirement is grounded or marked open
- alternatives are materially different
- each changed boundary has a concrete contract
- each behavior has an end-to-end call stack
- each contract maps to a file or open question
- the test plan proceeds in vertical red-green slices
- another engineer can implement without guessing architecture
