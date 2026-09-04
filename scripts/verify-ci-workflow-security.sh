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
        fail "$description PR verification job must reject fork repositories"
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

printf '%s\n' "$(extract_job "$BACKEND_WORKFLOW" check)" | grep -Fq 'runs-on: ubuntu-24.04' ||
    fail "Backend Verify must use the pinned GitHub-hosted runner"

printf '%s\n' "$(extract_job "$STACK_WORKFLOW" smoke)" | grep -Fq 'runs-on: ubuntu-24.04' ||
    fail "Stack Smoke must use the pinned GitHub-hosted runner"

verify_push_only_report_job \
    "$BACKEND_WORKFLOW" \
    "Backend Verify" \
    'runs-on: ubuntu-24.04'

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

echo "CI verification workflow token, routing, and secret boundaries verified"
