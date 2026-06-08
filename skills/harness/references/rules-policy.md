# Harness Rules Policy

Rules live in `.harness/rules/*.yaml`.

## Confirmation

Never add, delete, weaken, or strengthen a rule without explicit user confirmation. If a rule change is needed, set state to `needs_rule_confirmation` and present the exact YAML entry.

## Rule Levels

- `must`: required.
- `should`: default requirement; deviations need a recorded reason.
- `prefer`: project preference.
- `forbid`: prohibited action.

## Rule Quality

Rules should be concise, scoped, and checkable. Avoid broad values statements that cannot guide engineering behavior.
