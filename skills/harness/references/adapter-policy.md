# Harness Adapter Policy

Harness uses adapters for project-specific operations that cannot be universal across languages and frameworks.

## Adapter Files

- `.harness/adapters/verify.yaml`: commands or manual checks used to prove work.
- `.harness/adapters/testgen.yaml`: project testing locations and test-generation preferences.
- `.harness/adapters/repair.yaml`: repair limits and allowed validation commands.
- `.harness/adapters/pr.yaml`: PR base branch, clean-worktree expectations, and PR body requirements.

## Verification Contract

`harness-verify` must read `verify.yaml`, execute required stages and commands, record optional/skipped checks, write raw logs, and produce normalized verification reports. It must not invent framework-specific validation when the adapter exists.

## Stage Model

Prefer staged verification:

1. `unit`: fast local code-level tests. Do not start containers. Stop and repair when this stage fails.
2. `integration`: project-defined service integration. Start databases, services, containers, or local servers as needed.
3. `e2e`: browser, API, or user-flow tests against the integrated system.
4. `delivery`: final Harness gate that checks all required stage reports, skipped-test rationale, residual risks, and PR readiness.

Run stages in `stage_order`. Do not run a later required stage after an earlier required stage fails.

If verification coverage is insufficient, propose an adapter change and ask the user to confirm before writing it.

## Language

Adapter files stay in YAML and may use English keys. User-facing artifacts generated from adapters should use the user's working language.
