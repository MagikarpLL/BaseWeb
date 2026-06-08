---
name: harness-verify
description: Run the Harness verification phase through project-defined adapters, executing declared tests, checks, builds, scripts, or manual validation steps and recording normalized evidence. Use after implementation, before review, or when failed checks need structured evidence for repair.
---

# Harness Verify

Use this phase to prove whether implementation satisfies the spec. Harness Verify does not know every framework; it reads `.harness/adapters/verify.yaml` and treats project-specific commands as an adapter contract.

Verification is staged. Run cheap local checks first, then container-backed integration, then e2e behavior, then delivery gate. Do not proceed to a later required stage when an earlier required stage fails.

## Procedure

1. Read `.harness/state.yaml`, active spec acceptance criteria, `.harness/adapters/verify.yaml`, `.harness/gates/verify.gate.yaml`, and testing rules.
2. Resolve `stage_order`; default to `unit`, `integration`, `e2e`, `delivery` when absent.
3. Execute each required stage in order:
   - `unit`: code-level tests; do not start containers.
   - `integration`: start required services or containers and verify service/API connectivity.
   - `e2e`: browser/API/user-flow tests against the integrated system.
   - `delivery`: no implementation testing by default; verify prior required stage reports and skipped-test rationale.
4. Within the active stage, execute each required command unless it is marked `manual`.
5. For optional commands, run them when their declared files or scope apply; otherwise record them as skipped with a reason.
6. Stop at the first failed required stage, write evidence, set state to `verification_failed`, and prepare evidence for `harness-repair`.
7. Save raw logs under the active run or stage logs directory.
8. Write stage reports and normalized results to active run `verification.yaml` and `.harness/reports/verification-report.yaml`.
9. If all required stages pass, set state to `reviewing`.

## Evidence Format

Record:

- Command or manual check.
- Adapter name.
- Stage name.
- Result: pass, fail, skipped.
- Required: true or false.
- Exit code when a command ran.
- Log path when output exists.
- Important output summary.
- Failure category when known.
- Next recommended state.

## Guardrails

- Do not hide skipped tests; record why they were skipped.
- Do not invent commands when `verify.yaml` exists; propose adapter changes and ask for confirmation if verification coverage is insufficient.
- Do not mark verification passed when any required adapter check fails.
- Do not run integration, e2e, delivery, or PR gates after a required unit-stage failure.
- Do not run e2e or delivery gates after a required integration-stage failure.
- Do not repair in this phase unless the user explicitly invokes repair.
