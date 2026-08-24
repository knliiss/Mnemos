#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

fail() {
    echo "$1" >&2
    exit 1
}

verified_controllers=0

for source_file in $(find "$REPOSITORY_ROOT/apps" -path '*/src/main/kotlin/*' -type f -name '*.kt' -print); do
    if ! grep -Fq '@RequestMapping("/internal/' "$source_file"; then
        continue
    fi

    grep -Fq '@PreAuthorize(' "$source_file" ||
        fail "Internal API controller is missing caller authorization: ${source_file#"$REPOSITORY_ROOT/"}"

    grep -Fq 'authentication.name' "$source_file" ||
        fail "Internal API controller does not authorize by authenticated service identity: ${source_file#"$REPOSITORY_ROOT/"}"

    verified_controllers=$((verified_controllers + 1))
done

if [ "$verified_controllers" -eq 0 ]; then
    fail "No internal API controllers were verified"
fi

echo "Internal API caller authorization verified: $verified_controllers controllers"
