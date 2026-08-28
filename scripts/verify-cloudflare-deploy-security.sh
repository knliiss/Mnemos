#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/cloudflare-pages.yml"
FRONTEND_RUNNER='runs-on: [self-hosted, Linux, X64, mnemos-frontend]'

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
report_job="$(extract_job report)"

printf '%s\n' "$top_level_permissions" | grep -Fq '  contents: read' ||
    fail "Cloudflare workflow must keep top-level contents read permission"

if printf '%s\n' "$top_level_permissions" | grep -Eq '(deployments|statuses):[[:space:]]*write'; then
    fail "Cloudflare workflow must not grant deployment or status writes at workflow scope"
fi

printf '%s\n' "$build_job" | grep -Fq "if: github.ref == 'refs/heads/master'" ||
    fail "Cloudflare production build must be restricted to master"

printf '%s\n' "$deploy_job" | grep -Fq "if: github.ref == 'refs/heads/master'" ||
    fail "Cloudflare production deploy must be restricted to master"

printf '%s\n' "$report_job" | grep -Fq "github.ref == 'refs/heads/master'" ||
    fail "Cloudflare deployment status reporting must be restricted to master"

printf '%s\n' "$build_job" | grep -Fq "$FRONTEND_RUNNER" ||
    fail "Cloudflare production build must use the trusted frontend runner"

printf '%s\n' "$deploy_job" | grep -Fq "$FRONTEND_RUNNER" ||
    fail "Cloudflare production deploy must use the trusted frontend runner"

printf '%s\n' "$report_job" | grep -Fq "$FRONTEND_RUNNER" ||
    fail "Cloudflare deployment status reporting must use the trusted frontend runner"

if printf '%s\n' "$build_job" | grep -Fq 'CLOUDFLARE_API_TOKEN'; then
    fail "Cloudflare API token must not be exposed to the production build job"
fi

printf '%s\n' "$deploy_job" | grep -Fq 'deployments: write' ||
    fail "Cloudflare deploy job must own deployment write permission"

if printf '%s\n' "$deploy_job" | grep -Fq 'statuses: write'; then
    fail "Cloudflare deploy job must not receive status write permission"
fi

printf '%s\n' "$report_job" | grep -Fq 'statuses: write' ||
    fail "Cloudflare report job must own status write permission"

echo "Cloudflare production deployment security verified"
