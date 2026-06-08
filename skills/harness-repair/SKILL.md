---
name: harness-repair
description: Run bounded Harness self-repair using failed verification evidence, attempt tracking, minimal fixes, and repeated verification. Use when Harness verification fails and the workflow should attempt controlled repair without changing rules or scope.
---

# Harness Repair

Use this phase to fix failures demonstrated by verification evidence. Repair uses project adapter output as evidence; it does not guess failures from memory. Repair is stage-aware: fix the earliest failed required stage before rerunning later stages.

## Procedure

1. Read `.harness/state.yaml`, active run `verification.yaml`, `.harness/adapters/repair.yaml`, `.harness/adapters/verify.yaml`, and `.harness/repairs/attempts.yaml`.
2. Confirm failure evidence exists.
3. Check `gates.repair_attempt_limit`, default `2`.
4. If the limit is reached, set state to `repair_limit_reached`.
5. Otherwise, identify the failed stage from verification evidence: `unit`, `integration`, `e2e`, or `delivery`.
6. Create a minimal repair plan tied to one failure category in that stage.
7. Apply the smallest fix, increment attempts, and rerun only the failed stage first.
8. After the failed stage passes, rerun all subsequent required stages in order before declaring repair successful.
9. If all required stages pass, move to `reviewing`; otherwise record the failed attempt.

## Guardrails

- Do not repair without a failing test, build, check, or explicit validation failure.
- Do not change acceptance criteria to make a failure disappear.
- Do not edit rules without user confirmation.
- Do not exceed the repair attempt limit.
- Do not change verification adapters to make a failure pass unless the user explicitly confirms an adapter correction.
- Do not skip later required stages after repairing an earlier failed stage.
