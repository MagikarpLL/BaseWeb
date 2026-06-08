# Harness Workflow

Harness is a staged engineering workflow with persistent local state. It standardizes the deterministic phases and delegates project-specific validation to adapters.

## Phase Responsibilities

- Intake clarifies user intent and records open questions.
- Spec defines what must be true: problem, scope, acceptance criteria, design constraints, and test expectations.
- Plan defines how to execute: ordered changes, validation commands, risks, and repair strategy.
- Implement makes scoped changes from the plan.
- Verify records staged evidence from tests, builds, checks, or manual validation.
- Repair performs bounded fixes based on the earliest failed required verification stage.
- Review checks delivery readiness and residual risk.

## Adapter Boundary

Harness owns workflow, contracts, gates, state transitions, and normalized reports. Each project owns its verification implementation through `.harness/adapters/*.yaml`.

Do not try to create a universal test runner. Require every project to declare how it should be verified.

Use staged verification by default:

- `unit`: no containers; run local frontend/backend unit tests.
- `integration`: start required services/containers and verify API/service connectivity.
- `e2e`: run browser or user-flow checks against the integrated system.
- `delivery`: ensure all required stages passed before review or PR.

## Dispatcher Defaults

Use `harness` as the user's low-friction entrypoint. Dispatch to phase skills based on `.harness/state.yaml` and the user's immediate request. When state and user request conflict, honor the user but record the reason.

## Run Artifacts

For substantial work, create `.harness/runs/<timestamp>/` with:

- `request.md`
- `state-before.yaml`
- `plan.md`
- `changes.yaml`
- `verification.yaml`
- `repair-log.md`
- `review.md`
- `state-after.yaml`
