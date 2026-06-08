---
name: harness-state
description: Read, initialize, validate, recover, and advance Harness project state stored in .harness YAML files. Use when a Harness workflow needs state-machine handling, run folder creation, resume support, or transition validation across engineering phases.
---

# Harness State

Use this skill as the state-machine authority for Harness workflows.

## Procedure

1. Locate `.harness/state.yaml`.
2. If missing, initialize from `skills/harness/assets/harness-template/.harness/` when available.
3. Validate current state against known states.
4. Ensure active run and active spec are recorded for substantial work.
5. Check the relevant `.harness/gates/*.yaml` before advancing through spec, plan, verify, repair, review, or PR gates.
6. Advance only to allowed next states.
7. Record transitions in `transition_log`.

## Allowed States

`idle`, `intake`, `spec_drafting`, `spec_ready`, `planning`, `planned`, `implementing`, `verifying`, `repairing`, `reviewing`, `delivered`, `needs_user_input`, `needs_rule_confirmation`, `blocked`, `verification_failed`, `repair_limit_reached`.

## Guardrails

- Do not silently skip phase gates.
- Do not overwrite historical run artifacts.
- Preserve malformed state files for user recovery instead of deleting them.
