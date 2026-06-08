# Harness State Machine

The state file is `.harness/state.yaml`.

## Core States

`idle`, `intake`, `spec_drafting`, `spec_ready`, `planning`, `planned`, `implementing`, `verifying`, `repairing`, `reviewing`, `delivered`.

## Exceptional States

`needs_user_input`, `needs_rule_confirmation`, `blocked`, `verification_failed`, `repair_limit_reached`.

## Transition Rules

- `idle` -> `intake`
- `intake` -> `spec_drafting` or `needs_user_input`
- `spec_drafting` -> `spec_ready` or `needs_user_input`
- `spec_ready` -> `planning`
- `planning` -> `planned`
- `planned` -> `implementing`
- `implementing` -> `verifying`
- `verifying` -> `reviewing` or `verification_failed`
- `verification_failed` -> `repairing` or `repair_limit_reached`
- `repairing` -> `verifying`, `reviewing`, or `repair_limit_reached`
- `reviewing` -> `delivered` or the most specific incomplete phase

Record each transition in `transition_log` with timestamp, from, to, reason, and artifact.
