#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

fail() {
    echo "$1" >&2
    exit 1
}

verified_actions=0

for workflow in "$REPOSITORY_ROOT"/.github/workflows/*.yml "$REPOSITORY_ROOT"/.github/workflows/*.yaml; do
    [ -f "$workflow" ] || continue

    while IFS= read -r action_ref; do
        case "$action_ref" in
            '')
                continue
                ;;
            ./*)
                continue
                ;;
        esac

        revision="${action_ref##*@}"

        if ! printf '%s\n' "$revision" | grep -Eq '^[0-9a-f]{40}$'; then
            fail "Workflow action is not pinned to an immutable commit SHA: ${workflow#"$REPOSITORY_ROOT/"}: $action_ref"
        fi

        verified_actions=$((verified_actions + 1))
    done <<EOF
$(sed -n 's/^[[:space:]]*uses:[[:space:]]*\([^[:space:]#]*\).*/\1/p' "$workflow")
EOF
done

if [ "$verified_actions" -eq 0 ]; then
    fail "No workflow actions were verified"
fi

echo "Workflow action pins verified: $verified_actions actions"
