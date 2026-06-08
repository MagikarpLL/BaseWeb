---
name: harness-review
description: Run the Harness delivery review phase for implemented and verified work, checking risks, rule compliance, changed artifacts, residual gaps, and final state transition. Use before delivery or when a staged Harness task needs a final engineering review.
---

# Harness Review

Use this phase to decide whether work is ready to deliver. Write review artifacts in the user's working language unless the user asks otherwise.

## Procedure

1. Read `.harness/state.yaml`, active spec, plan, changes, verification evidence, `.harness/adapters/verify.yaml`, `.harness/gates/pr.gate.yaml`, `.harness/adapters/pr.yaml`, and rules.
2. Review for requirement fit, scope drift, missing tests, rule violations, and residual risk.
3. Write active run `review.md`.
4. If ready, set state to `delivered` and write `state-after.yaml`.
5. If not ready, set state to the most specific next state: `needs_user_input`, `planning`, `verifying`, or `repairing`.

## Guardrails

- Lead with actionable issues when reviewing code.
- Do not mark delivered if verification evidence is missing.
- Do not mark delivered if required verification adapter checks failed.
- Do not mark delivered unless all required verification stages passed or a skipped required stage has explicit user approval.
- Keep summaries short and tied to artifacts.
