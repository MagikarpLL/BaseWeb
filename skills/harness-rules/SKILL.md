---
name: harness-rules
description: Manage Harness rule files for engineering constraints, forbidden actions, testing gates, and self-repair policy. Use when rules must be read, interpreted, proposed, confirmed, or updated; all rule additions and modifications require explicit user confirmation before writing.
---

# Harness Rules

Use this skill to read and govern `.harness/rules/*.yaml`. Treat `.harness/adapters/*.yaml` changes as project behavior contract changes: propose exact edits and ask for confirmation before writing.

## Procedure

1. Read all relevant rules before spec, plan, implementation, verification, repair, or review.
2. When a new or changed rule or adapter is needed, present the exact proposed YAML to the user.
3. Do not write rule or adapter changes until the user confirms.
4. After confirmation, write the change and record it in the active run.
5. If confirmation is pending, set state to `needs_rule_confirmation`.

## Rule Shape

Use:

```yaml
- id: TEST-001
  level: must
  rule: "Delivery must include at least one recorded verification method."
  applies_when:
    - "Any implementation changes are delivered"
  confirmation_required_for_changes: true
  check:
    type: manual
```

Levels: `must`, `should`, `prefer`, `forbid`.

## Guardrails

- Do not create implicit rules from preference without confirmation.
- Do not weaken existing rules without confirmation.
- Do not weaken verification adapters to make failing work pass.
- Keep rules concise, testable when possible, and scoped.
