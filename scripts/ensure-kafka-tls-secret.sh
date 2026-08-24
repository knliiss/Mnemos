#!/usr/bin/env sh
set -eu

ENV_FILE="${1:-}"
KEY="KAFKA_TLS_STORE_PASSWORD"

if [ -z "$ENV_FILE" ]; then
    echo "Usage: $0 <env-file>" >&2
    exit 2
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "Compose env file does not exist: $ENV_FILE" >&2
    exit 1
fi

current_value="$(awk -F= -v key="$KEY" '$1 == key { value = substr($0, length(key) + 2) } END { print value }' "$ENV_FILE")"

if [ -n "$current_value" ]; then
    exit 0
fi

if ! command -v openssl >/dev/null 2>&1; then
    echo "openssl is required to generate $KEY" >&2
    exit 1
fi

value="$(openssl rand -hex 32)"
tmp_file="${ENV_FILE}.tmp.$$"

awk -v key="$KEY" -v value="$value" '
    BEGIN {
        replaced = 0
    }

    $0 ~ "^" key "=" {
        print key "=" value
        replaced = 1
        next
    }

    {
        print
    }

    END {
        if (!replaced) {
            print key "=" value
        }
    }
' "$ENV_FILE" > "$tmp_file"

mv "$tmp_file" "$ENV_FILE"
chmod 600 "$ENV_FILE" 2>/dev/null || true

echo "Generated missing $KEY in $ENV_FILE"
