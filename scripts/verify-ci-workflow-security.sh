#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
BACKEND_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/backend-verify.yml"
MINI_APP_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/mini-app.yml"
STACK_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/stack-smoke.yml"

fail() {
    echo "$1" >&2
    exit 1
}

extract_top_level_permissions() {
    workflow="$1"

    awk '
        /^permissions:$/ {
            in_permissions = 1
            next
        }

        in_permissions && /^[^[:space:]]/ {
            exit
        }

        in_permissions {
            print
        }
    ' "$workflow"
}

extract_job() {
    workflow="$1"
    job_name="$2"

    awk -v job_name="$job_name" '
        $0 == "  " job_name ":" {
            in_job = 1
        }

        in_job && /^  [A-Za-z0-9_-]+:$/ && $0 != "  " job_name ":" {
            exit
        }

        in_job {
            print
        }
    ' "$workflow"
}

verify_read_only_pr_job() {
    workflow="$1"
    job_name="$2"
    description="$3"

    top_level_permissions="$(extract_top_level_permissions "$workflow")"
    job="$(extract_job "$workflow" "$job_name")"

    printf '%s\n' "$top_level_permissions" | grep -Fq '  contents: read' ||
        fail "$description workflow must keep top-level contents read permission"

    if printf '%s\n%s\n' "$top_level_permissions" "$job" | grep -Fq 'statuses: write'; then
        fail "$description PR verification job must not receive status write permission"
    fi

    printf '%s\n' "$job" |
        grep -Fq "github.event.pull_request.head.repo.full_name == github.repository" ||
        fail "$description self-hosted PR job must reject fork repositories"
}

verify_push_only_report_job() {
    workflow="$1"
    description="$2"
    expected_runner="$3"

    report_job="$(extract_job "$workflow" report)"

    printf '%s\n' "$report_job" | grep -Fq "github.event_name == 'push'" ||
        fail "$description status report job must run only for push events"

    printf '%s\n' "$report_job" | grep -Fq "$expected_runner" ||
        fail "$description status report job must use its trusted runner boundary"

    printf '%s\n' "$report_job" | grep -Fq 'statuses: write' ||
        fail "$description status report job must own status write permission"
}

for workflow in "$BACKEND_WORKFLOW" "$MINI_APP_WORKFLOW" "$STACK_WORKFLOW"; do
    [ -f "$workflow" ] || fail "Verification workflow is missing: ${workflow#"$REPOSITORY_ROOT/"}"
done

verify_read_only_pr_job "$BACKEND_WORKFLOW" check "Backend Verify"
verify_read_only_pr_job "$MINI_APP_WORKFLOW" verify "Mini App Verify"
verify_read_only_pr_job "$STACK_WORKFLOW" smoke "Stack Smoke"

verify_push_only_report_job \
    "$BACKEND_WORKFLOW" \
    "Backend Verify" \
    'runs-on: [self-hosted, Linux, X64, mnemos-backend]'

verify_push_only_report_job \
    "$MINI_APP_WORKFLOW" \
    "Mini App Verify" \
    'runs-on: [self-hosted, Linux, X64, mnemos-frontend]'

BACKEND_WORKFLOW_CONTENT="$(cat "$BACKEND_WORKFLOW")"

printf '%s\n' "$BACKEND_WORKFLOW_CONTENT" | grep -Fq 'PUSH_BASE_SHA: ${{ github.event.before }}' ||
    fail "Backend Verify must resolve affected projects from the push base SHA"

printf '%s\n' "$BACKEND_WORKFLOW_CONTENT" | grep -Fq 'PUSH_HEAD_SHA: ${{ github.sha }}' ||
    fail "Backend Verify must resolve affected projects from the push head SHA"

printf '%s\n' "$BACKEND_WORKFLOW_CONTENT" | grep -Fq "printf '%s\\n' 'settings.gradle.kts' > \"\$changed_paths\"" ||
    fail "Backend Verify must fall back to a full backend verification when the event base SHA is unusable"

printf '%s\n' "$BACKEND_WORKFLOW_CONTENT" | grep -Fq "if: steps.backend_changes.outputs.dependency_scan_required == 'true'" ||
    fail "Backend dependency scanning must follow the affected-path resolver on both pushes and pull requests"

printf '%s\n' "$BACKEND_WORKFLOW_CONTENT" | grep -Fq "if: steps.backend_changes.outputs.check_tasks != ''" ||
    fail "Backend Gradle verification must follow the affected-project resolver on both pushes and pull requests"

if printf '%s\n' "$BACKEND_WORKFLOW_CONTENT" | grep -Fq "if: github.event_name == 'push' || steps.backend_changes.outputs.dependency_scan_required == 'true'"; then
    fail "Backend Verify must not force the dependency scan for every master push"
fi

if grep -Fq 'NVD_API_KEY: ${{ secrets.NVD_API_KEY }}' "$BACKEND_WORKFLOW"; then
    fail "Backend Verify must not expose the NVD API key unconditionally to pull requests"
fi

grep -Fq "NVD_API_KEY: \${{ github.event_name == 'push' && secrets.NVD_API_KEY || '' }}" "$BACKEND_WORKFLOW" ||
    fail "Backend Verify must expose the NVD API key only for push events"

STACK_WORKFLOW_CONTENT="$(cat "$STACK_WORKFLOW")"

stack_app_scope_count="$(
    printf '%s\n' "$STACK_WORKFLOW_CONTENT" |
        grep -Fc '      - "apps/**"' || true
)"
[ "$stack_app_scope_count" -eq 1 ] ||
    fail "Stack Smoke must run for all backend application changes only after they reach master"

stack_mini_app_exclusion_count="$(
    printf '%s\n' "$STACK_WORKFLOW_CONTENT" |
        grep -Fc '      - "!apps/mini-app/**"' || true
)"
[ "$stack_mini_app_exclusion_count" -eq 1 ] ||
    fail "Stack Smoke master trigger must exclude Mini App-only changes"

printf '%s\n' "$STACK_WORKFLOW_CONTENT" |
    grep -Fq '      - "apps/**/Dockerfile"' ||
    fail "Stack Smoke pull requests must still cover application image definition changes"

printf '%s\n' "$STACK_WORKFLOW_CONTENT" |
    grep -Fq '      - "apps/**/src/main/resources/**"' ||
    fail "Stack Smoke pull requests must still cover runtime configuration and migration changes"

printf '%s\n' "$STACK_WORKFLOW_CONTENT" |
    grep -Fq './gradlew assemble --console=plain --no-daemon' ||
    fail "Stack Smoke must assemble runtime artifacts without rerunning the full Gradle verification suite"

if printf '%s\n' "$STACK_WORKFLOW_CONTENT" | grep -Fq './gradlew clean build --console=plain --no-build-cache'; then
    fail "Stack Smoke must not repeat the full clean Gradle build after Backend Verify"
fi

if printf '%s\n' "$STACK_WORKFLOW_CONTENT" | grep -Fq '$CI_COMPOSE_COMMAND build --no-cache --quiet'; then
    fail "Stack Smoke must allow safe Docker layer reuse instead of forcing every image rebuild"
fi

echo "CI verification workflow token, routing, secret, and build-reuse boundaries verified"
