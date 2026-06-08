---
name: harness-implement
description: Execute a Harness implementation phase from an approved spec and plan while respecting project rules, scope boundaries, and persistent state. Use when Harness state is planned and the user wants the engineering changes made.
---

# Harness Implement

Use this phase to make the smallest code or artifact changes that satisfy the plan. Implementation must preserve the verification adapter contract instead of inventing new project test policy.

## Procedure

1. Read `.harness/state.yaml`, the active spec, the active plan, and relevant rules.
2. Confirm state is `planned` or explicitly approved for implementation.
3. Read `.harness/adapters/verify.yaml` so implementation can preserve expected validation paths.
4. Record `state-before.yaml` in the active run if it does not already exist.
5. Apply scoped changes following the plan.
6. Record changed files and rationale in active run `changes.yaml`.
7. Set state to `implementing` or `verifying` when changes are ready for adapter-based verification.

## Guardrails

- Do not broaden scope without returning to `harness-spec` or `harness-plan`.
- Do not edit rules without explicit user confirmation.
- Do not call the work complete without `harness-verify`.
- Preserve unrelated user changes.
