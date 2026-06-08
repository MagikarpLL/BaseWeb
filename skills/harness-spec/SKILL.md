---
name: harness-spec
description: Create and maintain Harness split specification documents that define what to build, why, boundaries, acceptance criteria, design constraints, and test expectations. Use after intake or whenever requirements need to be decomposed into stable specs before implementation planning.
---

# Harness Spec

Use this phase to define the correct target. A spec answers what must be true and why; it does not prescribe every implementation step. Write spec documents in the user's working language; Chinese input should produce Chinese specs unless the user requests English.

## Procedure

1. Read `.harness/state.yaml`, `.harness/specs/index.yaml`, and the current intake notes.
2. Update the split spec files and keep them aligned with `.harness/contracts/spec.contract.yaml`:
   - `000-intake.md`: raw request and clarification.
   - `001-problem.md`: problem, users, pain points.
   - `002-scope.md`: goals, non-goals, acceptance criteria.
   - `003-design.md`: technical shape, interfaces, constraints, risks.
   - `004-test-plan.md`: required verification scenarios.
   - `005-delivery.md`: delivery criteria and residual risks.
3. Ensure every acceptance criterion has a corresponding verification expectation in `004-test-plan.md`.
4. Update `.harness/specs/index.yaml` with active spec status and file mapping.
5. Mark state `spec_ready` only when scope and acceptance criteria are clear enough to plan and the spec gate can pass.

## Guardrails

- Keep implementation order out of the spec unless it is an externally imposed constraint.
- Preserve unresolved questions in the spec instead of hiding them.
- If a new rule seems necessary, stop and request confirmation through `harness-rules`.
