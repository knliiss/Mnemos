# Production emergency hotfix procedure

This procedure defines the repository-side preparation for an emergency Mnemos production hotfix.
It does not authorize production mutation. Any production runtime change requires a new explicit user instruction in live chat before an operator may execute it.

## Safety boundary

- Never hotfix by editing files, containers, databases, Keycloak, tunnels, or Docker services directly.
- Never use the removed incident-only recovery workflows.
- Never deploy an image identified only by a mutable tag.
- Never execute a transition whose recorded production baseline differs from the observed runtime.
- Never combine an application hotfix with a tooling revision change in the scoped executor.
- Never use a full-stack reconcile for a single-service hotfix.
- Never run production-mutating automation from a pull request branch.
- Keep `MNEMOS_PRODUCTION_EXECUTION_ENABLED` disabled except for a separately authorized execution window.

## 1. Prepare the fix

Create a dedicated hotfix branch from the intended source revision. Keep the code change minimal and run the normal safe verification for the affected service and its dependencies.

The commit that will become the production image must be a full 40-character repository commit SHA.

## 2. Build one immutable service image

Use `Production Service Image` manually for exactly one service and the full source SHA. The workflow may build and publish GHCR images but must not target the `mnemos-production` runner or mutate production runtime.

The resulting image identity is:

```text
<registry>/mnemos-<service>:<40-character-source-sha>
```

Do not replace the source SHA with `latest`, a branch name, or another mutable tag.

## 3. Record the current deployment state

Obtain the current durable production deployment state through the approved read-only observation path. Treat the state as immutable input to planning.

The state must validate with:

```bash
python3 scripts/production_deployment_state.py state-validate --input current-state.json
```

Do not hand-edit state IDs, release manifest IDs, or image references.

## 4. Generate a scoped hotfix plan

Run `Production Hotfix Plan` manually from `master`. Supply:

- the single deployable service;
- the full source SHA;
- the validated current deployment state JSON;
- a non-empty reason;
- no tooling revision change for a normal service hotfix.

The workflow is plan-only. Review the generated artifact before any production execution.

The plan must contain only `image-update` actions and should normally contain exactly one action for a service-local fix. Shared-library or contract changes may require a separately reviewed multi-service `Production Release Plan` instead of an emergency single-service hotfix.

Every planning artifact contains `audit-manifest.json`. Verify it before trusting the bundle:

```bash
python3 scripts/production_audit_bundle.py \
  verify \
  --directory <plan-directory> \
  --kind hotfix-plan
```

For a multi-service release plan, use `--kind release-plan`.

## 5. Review the transition

Before execution, verify all of the following:

- `mode` is `scoped`;
- every action names the expected runtime target;
- every `from.image` matches the recorded production baseline;
- every `to.image` is immutable and uses the intended source SHA;
- every rollback image is explicit;
- health/convergence checks are scoped to affected services;
- the transition ID matches the reviewed artifact;
- the tooling revision is unchanged;
- no full-runtime reconcile action is present;
- `audit-manifest.json` validates without missing, additional, resized, or modified files.

Keep the reviewed plan, current state, target state, transition, source SHA, actor, reason, planning workflow run ID, and artifact name together for auditability.

## 6. Obtain explicit production authorization

Repository preparation, CI success, a reviewed plan, and an available execution workflow do not authorize deployment.

Production execution requires a new explicit user instruction in live chat that authorizes the concrete production change. Without that instruction, stop after planning and artifact review.

For an authorized execution window:

1. confirm that the reviewed transition ID is the one named by the authorization;
2. enable `MNEMOS_PRODUCTION_EXECUTION_ENABLED` only for that execution window;
3. keep the executor's `MNEMOS_PRODUCTION_DEPLOY_ENABLED` and `MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED` gates under the same controlled authorization boundary;
4. require approval through the GitHub `production` environment before the production job may start;
5. disable `MNEMOS_PRODUCTION_EXECUTION_ENABLED` again after the authorized attempt finishes or is abandoned.

The repository workflow deliberately cannot infer live-chat authorization. The environment approval and temporary execution gate are the operational boundary that must represent that explicit authorization.

## 7. Execute only the reviewed scoped transition

Use `Production Targeted Execution` only after the authorization above exists. It must remain a manual `workflow_dispatch` workflow and must never gain an automatic trigger.

Supply the exact values from the reviewed plan:

- successful planning workflow run ID;
- exact artifact name;
- plan kind (`hotfix-plan` or `release-plan`);
- full tooling revision recorded by both sides of the transition;
- exact `sha256:` transition ID;
- authorization text exactly equal to `APPLY <transition-id>`;
- the live-chat authorization reference/reason.

The workflow independently verifies that the plan came from a successful manually dispatched `Production Hotfix Plan` or `Production Release Plan` run on `master`. It downloads that exact run artifact, verifies `audit-manifest.json`, verifies the pinned tooling revision and transition ID, rejects non-scoped plans, and renders the executor dry-run before the execution step.

When separately authorized, the scoped executor runs with all execution gates enabled, as root, under its exclusive deployment lock, and with `--expected-transition-id` equal to the reviewed transition ID.

The executor must:

1. acquire the global scoped-deployment lock before reading current state;
2. verify the live runtime baseline for every affected service;
3. update only the actions in the reviewed transition;
4. wait for service-scoped convergence/health;
5. automatically roll back already attempted actions if an apply step fails;
6. persist the execution attempt and resulting state atomically.

A baseline mismatch is a hard stop. Do not bypass it by regenerating state from the desired result.

## 8. Failure and rollback

If apply fails, the executor uses the rollback image recorded in the transition and checks convergence again. A successful automatic rollback leaves the target state unapplied and records a `rolled-back` execution attempt.

If rollback fails, stop all further deployment work. Preserve the `rollback-failed` execution record and investigate the affected service. Do not widen the change into a full-stack recovery without a separately reviewed plan and explicit authorization.

Do not retry by reusing an unreviewed replacement artifact or changing the expected transition ID. A new target requires a new plan and a new explicit authorization.

## 9. Post-hotfix audit

After an authorized deployment attempt, retain:

- immutable source SHA and image identity;
- release manifest and state IDs;
- transition ID;
- actor and authorization reason;
- planning workflow run ID and artifact name;
- execution workflow run ID;
- execution timestamps and attempted services;
- CI/build links and integrity-verified plan artifact;
- final scoped health/convergence result;
- immutable executor attempt records.

`Production Targeted Execution` packages the available state and transition attempt records into a tamper-evident execution audit artifact with 90-day retention.

Temporary incident-only workflows are not part of the supported process and must remain removed.
