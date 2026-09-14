---
name: coding-standards
description: Applies language-neutral engineering standards when writing or reviewing code, especially around boundaries, failures, domain models, dependencies, tests, and type safety.
metadata:
  category: development
---

# Coding Standards

Inspect the repository before introducing patterns, libraries, or abstractions. Follow compatible local conventions; contain incompatible legacy patterns at the nearest boundary instead of spreading them into new code.

The examples use TypeScript syntax, but the principles apply across languages. Adapt them to the language and ecosystem already in use.

## Priorities

1. Correctness, safety, and debuggability.
2. Explicit domain meaning and dependencies.
3. Compatibility with sound project conventions.
4. The narrowest change that fully solves the requested behavior.
5. Documentation for surprising, durable trade-offs.

## Parse at boundaries

Turn unknown or loosely shaped input into application and domain types as early as possible.

```text
unknown -> protocol input -> parser -> domain/application input
```

A parser returns the refined value or an explicit parsing failure. Do not validate input and then continue carrying the original primitive or transport shape.

Use constrained domain types where they prevent realistic mistakes: identifiers, URLs, email addresses, money, units, constrained numbers, and non-empty values. Construct them through parsers or smart constructors.

## Expected failures are values

Known failures belong in explicit return types or equivalent language constructs. This includes parsing, authorization, configuration, persistence, I/O, integration, and workflow failures.

```ts
type Result<T, E extends Error> =
  | { readonly _tag: "ok"; readonly value: T }
  | { readonly _tag: "err"; readonly error: E };
```

Translate dependency-specific exceptions inside the adapter that owns that dependency. Reserve throws, panics, or equivalent mechanisms for defects and impossible internal states. At entrypoints, turn expected failures into valid protocol outcomes: responses, exit codes, retry decisions, dead letters, or startup messages.

Errors should have a stable category, useful safe context, and preserve the original cause where the language supports it. Never put secrets or raw credentials in errors, traces, snapshots, or logs.

## Model meaningful states

Make illegal states hard to construct. Prefer explicit variants over boolean combinations and nullable bags.

```ts
type Invoice =
  | { readonly _tag: "draft"; readonly id: InvoiceId }
  | { readonly _tag: "sent"; readonly id: InvoiceId; readonly sentAt: Instant }
  | { readonly _tag: "paid"; readonly id: InvoiceId; readonly paidAt: Instant };
```

Avoid boolean parameters that control behavior. Use named options, enums, variants, or domain values.

## Functional core, imperative shell

Separate business decisions from side effects when they are tangled together.

- **Functional core:** pure logic that operates only on its inputs and returns data. It does not read clocks, databases, environment variables, random generators, networks, or mutable global state.
- **Imperative shell:** acquires inputs, invokes the core, and performs effects such as persistence, network calls, logging, and message delivery.

Do not mix policy and I/O in the same loop:

```ts
// Logic, time, storage, and email delivery are coupled.
function sendUserExpiryEmail(): void {
  for (const user of db.getUsers()) {
    if (user.subscriptionEndDate > Date.now()) continue;
    if (user.isFreeTrial) continue;
    email.send(user.email, `Your account has expired ${user.name}.`);
  }
}
```

Move decisions and output construction into the functional core:

```ts
type ExpiryEmail = { readonly to: string; readonly body: string };

function getExpiredUsers(
  users: readonly User[],
  cutoff: number,
): readonly User[] {
  return users.filter(
    (user) => user.subscriptionEndDate <= cutoff && !user.isFreeTrial,
  );
}

function generateExpiryEmails(users: readonly User[]): readonly ExpiryEmail[] {
  return users.map((user) => ({
    to: user.email,
    body: `Your account has expired ${user.name}.`,
  }));
}
```

Keep the imperative shell thin and explicit:

```ts
const users = db.getUsers();
const cutoff = Date.now();
const expiredUsers = getExpiredUsers(users, cutoff);
const messages = generateExpiryEmails(expiredUsers);
email.bulkSend(messages);
```

Pass changing context such as time into the core as data. Prefer returning decisions, commands, or messages for the shell to execute. This keeps policy reusable and lets most behavior be tested without mocks. Do not force trivial orchestration into artificial abstractions; separate effects where doing so clarifies meaningful logic.

Further reading: [Simplify Your Code: Functional Core, Imperative Shell](https://testing.googleblog.com/2025/10/simplify-your-code-functional-core.html) by Arham Jain.

## Module responsibilities

Use these roles when the behavior needs them; do not create layers to satisfy a diagram.

- **Domain module:** pure meanings, invariants, calculations, and legal transitions.
- **Application service:** authorization, policy, and sequencing across explicit ports.
- **Adapter:** framework, protocol, storage, runtime, or vendor translation.
- **Composition root:** configuration, resource acquisition, concrete wiring, and lifecycle.

Dependencies point inward. Domain code knows no framework or adapter. Application services depend on narrow application-owned ports, not SDK or database types. Adapters translate external types and failures at the edge.

Prefer deep cohesive modules: substantial behavior behind a small, meaningful interface. Avoid pass-through wrappers, repository-per-table defaults, vague managers or helpers, and abstractions created for one call site.

Before creating a service or adapter:

1. reuse an existing cohesive implementation through a narrow port;
2. extend it when the new behavior changes for the same reason;
3. create a new boundary only when reuse would create bad coupling.

## Effects and workflows

Make effects and dependencies visible. Pass clocks, randomness, storage, and external clients through explicit parameters, interfaces, capabilities, or the ecosystem's established dependency mechanism.

Use ordinary calls and transactions for short single-boundary work. Use durable workflows only when progress must survive crashes, redelivery, long delays, compensation, human approval, or multiple transaction boundaries.

Any externally visible mutation that may be retried needs an explicit idempotency strategy. Do not hold database transactions open across network calls.

## Testing

Test behavior through public seams.

Prefer, in order:

1. end-to-end tests through real entrypoints;
2. integration tests through real boundaries;
3. focused or property tests for pure domain modules;
4. unit tests when they verify behavior rather than implementation.

Avoid module mocks, private-method tests, call-count assertions, and tautological expected values. Mock external boundaries only. Use test databases or local implementations when persistence semantics matter.

## Type safety

Use the language's type system and static analysis to prevent realistic mistakes. Enable strong checking where practical and follow the repository's established safety settings.

Prefer immutable inputs and outputs. Avoid unchecked casts, forced null access, dynamic escape hatches, and suppressed diagnostics. When the type system cannot express a proven invariant, isolate the escape hatch and document why it is sound.

Document contracts, invariants, and failure behavior—not syntax already visible in the code.

## Completion checklist

- inputs are parsed at the edge
- domain states and identifiers are explicit
- expected failures are represented explicitly
- external types remain inside adapters
- business decisions are separated from side effects where useful
- dependencies are passed through explicit boundaries
- existing modules were checked before adding new ones
- tests observe behavior through agreed public seams
- logs and errors contain no secrets
- unsafe escapes and abstractions have a concrete justification
