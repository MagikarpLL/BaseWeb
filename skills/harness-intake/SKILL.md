---
name: harness-intake
description: Run the Harness intake phase for requirement clarification, problem framing, open-question capture, user intent recovery, and transition into spec drafting. Use when a Harness workflow starts with unclear requirements or needs explicit goal, scope, audience, constraints, or success criteria before planning or implementation.
---

# Harness Intake

Clarify the user's request before spec, plan, or implementation work.

## Procedure

1. Read `.harness/state.yaml` if present. If missing, ask the dispatcher or user to initialize Harness state.
2. Capture the raw request in `.harness/specs/000-intake.md` or the active run `request.md`.
3. Identify goal, audience, success criteria, non-goals, constraints, and unknowns.
4. Ask only questions that materially change the spec or prevent risky assumptions.
5. If enough intent is known, set the next state to `spec_drafting`.
6. If not enough intent is known, set state to `needs_user_input` and record `open_questions`.

## Output Contract

Produce:

- A concise clarified goal.
- A list of confirmed constraints.
- A list of open questions, if any.
- A recommendation for whether to move to `harness-spec`.

## Guardrails

- Do not propose implementation details until the user intent is stable.
- Do not edit rules during intake; propose rule changes through `harness-rules`.
- Do not read or rely on project `history-doc` material.
