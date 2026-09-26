#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
SOURCE_ENV="$REPOSITORY_ROOT/.env.example"
TARGET_ENV="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"
TARGET_DIR="$(dirname -- "$TARGET_ENV")"
SECRET_VALIDATOR="$REPOSITORY_ROOT/scripts/validate-compose-secrets.sh"

if [ -e "$TARGET_ENV" ]; then
    echo "Production environment already exists: $TARGET_ENV" >&2
    echo "Refusing to overwrite existing production secrets." >&2
    exit 1
fi

if ! command -v openssl >/dev/null 2>&1; then
    echo "openssl is required" >&2
    exit 1
fi

install -d -o root -g root -m 0700 "$TARGET_DIR"
install -o root -g root -m 0600 "$SOURCE_ENV" "$TARGET_ENV"

TEMP_DIR="$(mktemp -d)"

cleanup() {
    rm -rf "$TEMP_DIR"
}

trap cleanup EXIT INT TERM

set_env() {
    key="$1"
    value="$2"
    temp_file="$TEMP_DIR/env"

    awk -v key="$key" -v value="$value" '
        BEGIN { found = 0 }
        index($0, key "=") == 1 {
            if (!found) {
                print key "=" value
                found = 1
            }
            next
        }
        { print }
        END {
            if (!found) {
                print key "=" value
            }
        }
    ' "$TARGET_ENV" > "$temp_file"

    install -o root -g root -m 0600 "$temp_file" "$TARGET_ENV"
}

generate_secret() {
    key="$1"
    set_env "$key" "$(openssl rand -hex 32)"
}

set_env "MNEMOS_FRONTEND_ORIGIN" "https://app.knalis.rest"
set_env "MNEMOS_API_ORIGIN" "https://api.knalis.rest"
set_env "MNEMOS_AUTH_ORIGIN" "https://auth.knalis.rest"
set_env "MNEMOS_HOST_BIND_ADDRESS" "127.0.0.1"
set_env "TELEGRAM_BOT_ENABLED" "false"
set_env "CRYSTALRUN_NOTIFICATION_API_KEY" "crystalrun-internal-v1"

for key in $(sh "$SECRET_VALIDATOR" --list-generated); do
    generate_secret "$key"
done

sh "$REPOSITORY_ROOT/scripts/generate-kafka-message-keys.sh" "$TARGET_ENV"

chmod 600 "$TARGET_ENV"
chown root:root "$TARGET_ENV"

cat <<EOF
Production environment initialized at:
  $TARGET_ENV

Internal passwords, API keys, Kafka credentials and Ed25519 signing keys were generated locally.
No generated secret was printed.

External values still required before first deployment:
  MNEMOS_TELEGRAM_BOT_TOKEN
  MNEMOS_SUPER_ADMIN_TELEGRAM_ID
  CRISTALIX_INTERACTIVE_PROJECT_TOKEN
  CRISTALIX_INTERACTIVE_PROJECT_KEY
  CRISTALIX_MONITORING_PROJECT_TOKEN
  CRISTALIX_MONITORING_PROJECT_KEY
  CRISTALIX_RESERVE_PROJECT_TOKEN
  CRISTALIX_RESERVE_PROJECT_KEY
EOF
