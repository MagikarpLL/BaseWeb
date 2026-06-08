# Harness Spec Policy

Harness specs are split so the workflow can update one concern without rewriting all project intent.

Write specs in the user's working language. If the request is Chinese, generate Chinese spec documents unless the user explicitly asks for another language.

## Files

- `000-intake.md`: raw request, clarified intent, open questions.
- `001-problem.md`: user, pain point, why the work matters.
- `002-scope.md`: goals, non-goals, acceptance criteria.
- `003-design.md`: architecture, interfaces, data flow, constraints, risks.
- `004-test-plan.md`: automated and manual verification expectations.
- `005-delivery.md`: delivery checklist, evidence, residual risks.

## Rules

- Put what and why in spec.
- Put execution order in plan.
- Keep unresolved questions visible.
- Move to `spec_ready` only when acceptance criteria are clear enough to verify.
- Every acceptance criterion should map to at least one verification expectation. The concrete command can remain project-specific and live in `.harness/adapters/verify.yaml`.
