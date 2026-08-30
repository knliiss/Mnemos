#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/cloudflare-pages.yml"
FRONTEND_RUNNER='runs-on: [self-hosted, Linux, X64, mnemos-frontend]'
DEPLOY_BRANCH='deploy/production-frontend'

fail() {
    echo "$1" >&2
    exit 1
}

extract_job() {
    job_name="$1"

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
    ' "$WORKFLOW"
}

[ -f "$WORKFLOW" ] || fail "Cloudflare Pages workflow is missing"

if grep -Eq '^  (pull_request|workflow_run|schedule|repository_dispatch):' "$WORKFLOW"; then
    fail "Cloudflare production deployment contains an unsupported automatic trigger"
fi

grep -Fq '  workflow_dispatch:' "$WORKFLOW" ||
    fail "Cloudflare production deployment must keep workflow_dispatch"

grep -Fq '  push:' "$WORKFLOW" ||
    fail "Cloudflare connector-driven deployment trigger is missing"

grep -Fq "      - $DEPLOY_BRANCH" "$WORKFLOW" ||
    fail "Cloudflare push deployment must remain restricted to $DEPLOY_BRANCH"

if awk '
    /^  push:$/ {
        in_push = 1
        next
    }

    in_push && /^  [A-Za-z0-9_-]+:$/ {
        exit
    }

    in_push && /^      - / {
        count += 1
    }

    END {
        exit count == 1 ? 0 : 1
    }
' "$WORKFLOW"; then
    :
else
    fail "Cloudflare push deployment must target exactly one trusted branch"
fi

grep -Fq '  group: cloudflare-pages-production' "$WORKFLOW" ||
    fail "Cloudflare production deployment must keep global concurrency"

grep -Fq '  cancel-in-progress: false' "$WORKFLOW" ||
    fail "Cloudflare production deployment must not cancel an in-flight production deployment"

top_level_permissions="$(awk '
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
' "$WORKFLOW")"

build_job="$(extract_job build)"
deploy_job="$(extract_job deploy)"

printf '%s\n' "$top_level_permissions" | grep -Fq '  contents: read' ||
    fail "Cloudflare workflow must keep top-level contents read permission"

if printf '%s\n' "$top_level_permissions" | grep -Eq '(deployments|statuses):[[:space:]]*write'; then
    fail "Cloudflare workflow must not grant deployment or status writes at workflow scope"
fi

printf '%s\n' "$build_job" | grep -Fq "$FRONTEND_RUNNER" ||
    fail "Cloudflare production build must use the trusted frontend runner"

printf '%s\n' "$deploy_job" | grep -Fq "$FRONTEND_RUNNER" ||
    fail "Cloudflare production deploy must use the trusted frontend runner"

printf '%s\n' "$deploy_job" | grep -Fq 'environment: production' ||
    fail "Cloudflare production deploy must require the production environment boundary"

if printf '%s\n' "$build_job" | grep -Fq 'CLOUDFLARE_API_TOKEN'; then
    fail "Cloudflare API token must not be exposed to the production build job"
fi

grep -Fq "SOURCE_SHA: \${{ github.event_name == 'workflow_dispatch' && inputs.source_sha || github.sha }}" "$WORKFLOW" ||
    fail "Cloudflare production source SHA must be derived safely for manual and connector-driven deploys"

printf '%s\n' "$build_job" | grep -Fq 'git merge-base --is-ancestor "$SOURCE_SHA" origin/master' ||
    fail "Cloudflare manual source SHA must be verified against trusted master history"

printf '%s\n' "$build_job" | grep -Fq 'DEPLOY $SOURCE_SHA' ||
    fail "Cloudflare manual production deployment must require exact source-SHA confirmation"

printf '%s\n' "$build_job" | grep -Fq 'refs/heads/deploy/production-frontend' ||
    fail "Cloudflare connector-driven deployment must verify the trusted deployment ref"

printf '%s\n' "$build_job" | grep -Fq 'master_sha="$(git rev-parse origin/master)"' ||
    fail "Cloudflare connector-driven deployment must resolve the current trusted master HEAD"

printf '%s\n' "$build_job" | grep -Fq '[ "$SOURCE_SHA" != "$master_sha" ]' ||
    fail "Cloudflare connector-driven deployment must reject non-master source revisions"

printf '%s\n' "$deploy_job" | grep -Fq 'PRODUCTION_DEPLOY_ENABLED: ${{ vars.MNEMOS_CLOUDFLARE_PRODUCTION_DEPLOY_ENABLED }}' ||
    fail "Cloudflare production deploy must read its deployment gate from the production environment"

printf '%s\n' "$deploy_job" | grep -Fq '[ "$PRODUCTION_DEPLOY_ENABLED" != "true" ]' ||
    fail "Cloudflare production deploy must enforce the explicit production deployment gate"

printf '%s\n' "$deploy_job" | grep -Fq 'deployments: write' ||
    fail "Cloudflare deploy job must own deployment write permission"

if grep -Eq -- '--request[[:space:]]+(POST|PATCH|PUT|DELETE)|dns_records|pages/projects/.*/domains' "$WORKFLOW"; then
    fail "Cloudflare Pages workflow must not mutate project, domain, or DNS configuration"
fi

if grep -Eq '^  report:' "$WORKFLOW"; then
    fail "Cloudflare production deployment must not publish mutable commit status from the production runner"
fi

echo "Cloudflare production deployment security verified"
