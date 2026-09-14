---
name: type-breakdown
description: Maps a code path's types, abstractions, data flow, side effects, and errors. Use only when the user explicitly asks for a type breakdown of a feature, flow, or code path.
metadata:
  category: development
---

# Type Breakdown

Walk the user through the requested code paths, types, abstractions, and data flow.

Turn a broad plan into a concrete, type-driven implementation outline. Prefer pseudocode, type signatures, and execution structure over PRD-style prose.

Inspect the existing code before proposing new abstractions.

If the subject is simple, keep the walkthrough concise. Otherwise, use the structure below as a reference. Do not follow it rigidly when a section does not apply.

Use these markers:

```text
[existing]  Confirmed in the codebase
[inferred]  Implied by the current implementation
[proposed]  Introduced by this plan
[?]         Unresolved
```

## Execution Tree

Start from the entry point and expand the call path recursively in execution order.

For every relevant call, show:

- Fully qualified function, method, class, or module name
- File where it is defined
- Parent file and call site
- Responsibility
- Input and output types
- Data transformations
- Side effects
- Possible errors
- Child calls

Use this format:

```text
▼ [existing] CheckoutController.create
  defined: src/checkout/api/CheckoutController.ts:24
  input:   CreateCheckoutRequest
  output:  Promise<HttpResponse<CreateCheckoutResponse>>

  └─▶ [existing] CreateCheckout.execute
      defined: src/checkout/application/CreateCheckout.ts:18
      called:  CheckoutController.ts:42
      input:   CreateCheckoutCommand
      output:  Result<Checkout, CreateCheckoutError>
      effects: reads customer, writes checkout

      ├─▶ CustomerRepository.find
      ├─▶ Checkout.create
      └─▶ CheckoutRepository.save
```

Use `× N` for repeated calls over collections. Do not invent line numbers when they are unavailable.

## Type Flow

Show how data becomes progressively more constrained:

```text
unknown
  → CreateCheckoutRequest
  → CreateCheckoutCommand
  → ResolvedCheckoutInput
  → Checkout
  → CheckoutRow
  → CreateCheckoutResponse
```

Identify the validation or transformation responsible for each transition.

## Type Definitions

Provide pseudocode definitions for the important boundary, application, domain, persistence, state, and error types.

```ts
type CreateCheckoutCommand = {
  customerId: CustomerId;
  items: NonEmptyArray<CheckoutItemInput>;
};

type CreateCheckoutError =
  | { type: "CustomerNotFound"; customerId: CustomerId }
  | { type: "InvalidCheckout"; cause: CheckoutValidationError }
  | { type: "PersistenceFailure"; cause: PersistenceError };
```

Prefer domain-specific types over primitive strings and numbers when they encode identity, validation, units, or invariants.

## Error Flow

Show where each error originates and how it is mapped and propagated:

```text
UniqueConstraintViolation
  → PersistenceError.Conflict
  → CreateCheckoutError.PersistenceFailure
  → HTTP 409
```

## Implementation Outline

Finish with:

1. Proposed functions and type signatures
2. Type ownership by file or module
3. Existing code that must change
4. New files or abstractions required
5. Unresolved design questions

Keep the output concrete, source-aware, and directly reviewable as an implementation plan.
