---
name: harness
description: Orchestrate Harness staged engineering work with persistent .harness state, split specs, plans, verification, repair loops, reviews, and rule governance. Use when the user asks to run a Harness workflow, coordinate multiple Harness stages, initialize project Harness state, resume staged engineering work, or lower usage cost through one dispatcher skill.
---

# Harness

Use this as the dispatcher for Harness engineering work. It coordinates deterministic workflow phases while delegating project-specific verification to `.harness/adapters/*.yaml`.

Harness owns: intake, spec, plan, state, gates, artifact tracking, repair limits, review, and PR readiness. The project owns: framework-specific test commands, build commands, environment setup, and any custom validation scripts.

## Dispatch Flow

1. Locate the project root and check for `.harness/state.yaml`.
2. If `.harness/` is missing, initialize it from `assets/harness-template/.harness/`.
3. Read `.harness/state.yaml`, `.harness/specs/index.yaml`, `.harness/adapters/*.yaml`, `.harness/gates/*.yaml`, and all applicable `.harness/rules/*.yaml`.
4. Select the next phase:
   - `idle` or `needs_user_input`: use `harness-intake`.
   - `intake` or `spec_drafting`: use `harness-spec`.
   - `spec_ready`: use `harness-plan`.
   - `planned`: use `harness-implement`.
   - `implementing` or `verification_failed`: use `harness-verify`.
   - `repairing` or failed verification within the attempt limit: use `harness-repair`.
   - `verifying`: use `harness-review`.
5. Create a run folder under `.harness/runs/<timestamp>/` for every substantial request.
6. Record before/after state, request, plan, verification, repairs, and review artifacts as applicable.
7. Advance state only after the current phase produces its required artifact and the relevant gate allows the transition.

## Core Rules

- Keep `spec` and `plan` separate: spec defines what must be true; plan defines how to make it true.
- Do not implement before a sufficient spec exists unless the user explicitly asks for a tiny exploratory change.
- Do not deliver code changes before recording verification evidence.
- Do not edit `.harness/rules/*.yaml` without asking the user to confirm the new or changed rule.
- Do not perform repair without failed verification evidence.
- Limit automatic repair attempts to `.harness/state.yaml` `gates.repair_attempt_limit`, default `2`.
- Generate user-facing specs, plans, questions, and reviews in the user's working language. If the user writes Chinese, produce Chinese Harness artifacts unless the user asks otherwise.
- Treat verification as an adapter protocol. Do not invent framework-specific commands when `.harness/adapters/verify.yaml` is present.

## References

- Read `references/workflow.md` for phase responsibilities.
- Read `references/state-machine.md` before changing `.harness/state.yaml`.
- Read `references/spec-policy.md` before creating or restructuring specs.
- Read `references/rules-policy.md` before proposing rule changes.
- Read `references/adapter-policy.md` before verification, repair, test generation, or PR automation.
