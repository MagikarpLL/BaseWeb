---
name: harness-plan
description: Convert a Harness spec into an executable engineering plan with ordered steps, validation strategy, risk controls, and phase gates. Use when a spec is ready and work needs a concrete implementation path without changing files yet.
---

# Harness Plan

Use this phase to decide how to build the spec safely. Write plans in the user's working language unless the user asks otherwise.

## Procedure

1. Read `.harness/state.yaml`, the active spec files, and relevant rules.
2. Derive a compact implementation plan from acceptance criteria.
3. Include expected touched areas, sequence, validation adapter names, and failure handling.
4. Write the plan to the active run `plan.md` or equivalent planning artifact.
5. Confirm `.harness/adapters/verify.yaml` defines at least one required verification command or manual check before implementation proceeds.
6. Set state to `planned` when the plan is decision-complete enough for implementation.

## Plan Contents

Include:

- Implementation summary.
- Ordered work steps.
- Verification adapter coverage for acceptance criteria.
- Known risks and rollback or repair strategy.
- Assumptions that remain after spec.

## Guardrails

- Do not edit implementation files in this phase.
- Do not invent rules; propose them through `harness-rules`.
- Keep plan specific enough that `harness-implement` does not need to make major decisions.
