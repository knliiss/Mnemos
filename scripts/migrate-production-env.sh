#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
TARGET_ENV="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"
VALIDATOR="$REPOSITORY_ROOT/scripts/validate-production-env.sh"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Production environment does not exist: $TARGET_ENV" >&2
    exit 1
fi

if ! command -v openssl >/dev/null 2>&1; then
    echo "openssl is required" >&2
    exit 1
fi

if [ ! -f "$VALIDATOR" ]; then
    echo "Production environment validator does not exist: $VALIDATOR" >&2
    exit 1
fi

duplicate_keys="$(
    awk -F= '
        /^[A-Za-z_][A-Za-z0-9_]*=/ {
            count[$1]++
        }
        END {
            for (key in count) {
                if (count[key] > 1) {
                    print key
                }
            }
        }
    ' "$TARGET_ENV" | sort
)"

if [ -n "$duplicate_keys" ]; then
    echo "Production environment contains duplicate variables:" >&2
    printf '%s\n' "$duplicate_keys" >&2
    exit 1
fi

TEMP_DIR="$(mktemp -d)"
WORKING_ENV="$TEMP_DIR/mnemos.env"

cleanup() {
    rm -rf "$TEMP_DIR"
}

trap cleanup EXIT INT TERM

cp "$TARGET_ENV" "$WORKING_ENV"

has_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            found = 1
            exit
        }
        END {
            exit found ? 0 : 1
        }
    ' "$WORKING_ENV"
}

get_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$WORKING_ENV"
}

set_env() {
    key="$1"
    value="$2"
    next_env="$TEMP_DIR/next.env"

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
    ' "$WORKING_ENV" > "$next_env"

    mv "$next_env" "$WORKING_ENV"
}

ensure_default() {
    key="$1"
    value="$2"

    if ! has_env "$key"; then
        set_env "$key" "$value"
    fi
}

ensure_generated_secret() {
    key="$1"

    if has_env "$key"; then
        value="$(get_env "$key")"

        case "$value" in
            ""|change_me|change_me_*|replace_me|replace_me_*|changeme|replace-me|replace_me_personalization)
                ;;
            *)
                return
                ;;
        esac
    fi

    set_env "$key" "$(openssl rand -hex 32)"
}

migrate_legacy_secret() {
    canonical_key="$1"
    legacy_key="$2"

    if has_env "$canonical_key"; then
        canonical_value="$(get_env "$canonical_key")"

        case "$canonical_value" in
            ""|change_me|change_me_*|replace_me|replace_me_*|changeme|replace-me|replace_me_personalization)
                ;;
            *)
                return
                ;;
        esac
    fi

    if ! has_env "$legacy_key"; then
        return
    fi

    legacy_value="$(get_env "$legacy_key")"

    case "$legacy_value" in
        ""|change_me|change_me_*|replace_me|replace_me_*|changeme|replace-me|replace_me_personalization)
            return
            ;;
    esac

    set_env "$canonical_key" "$legacy_value"
}

if ! has_env TELEGRAM_BOT_ENABLED; then
    if has_env NOTIFICATION_TELEGRAM_ENABLED; then
        set_env TELEGRAM_BOT_ENABLED "$(get_env NOTIFICATION_TELEGRAM_ENABLED)"
    else
        set_env TELEGRAM_BOT_ENABLED "false"
    fi
fi

ensure_default TELEGRAM_BOT_SERVICE_VERSION local
ensure_default TELEGRAM_BOT_MENU_SESSION_TTL 30d

migrate_legacy_secret TELEGRAM_BOT_SERVICE_BET_API_KEY BET_SERVICE_TELEGRAM_BOT_API_KEY

NEW_GENERATED_SECRETS="
REDIS_TELEGRAM_BOT_PASSWORD
ACCOUNT_SERVICE_TELEGRAM_BOT_API_KEY
WALLET_SERVICE_TELEGRAM_BOT_API_KEY
NOTIFICATION_SERVICE_ACCOUNT_API_KEY
NOTIFICATION_SERVICE_EVENT_API_KEY
NOTIFICATION_SERVICE_TELEGRAM_BOT_API_KEY
TELEGRAM_BOT_SERVICE_ACCOUNT_API_KEY
TELEGRAM_BOT_SERVICE_BET_API_KEY
TELEGRAM_BOT_SERVICE_EVENT_API_KEY
TELEGRAM_BOT_SERVICE_NOTIFICATION_API_KEY
TELEGRAM_BOT_SERVICE_PAYMENT_API_KEY
TELEGRAM_BOT_SERVICE_PERSONALIZATION_API_KEY
TELEGRAM_BOT_SERVICE_PLAYER_DIRECTORY_API_KEY
TELEGRAM_BOT_SERVICE_REWARD_API_KEY
TELEGRAM_BOT_SERVICE_WALLET_API_KEY
REWARD_SERVICE_TELEGRAM_BOT_API_KEY
PAYMENT_SERVICE_ACCOUNT_API_KEY
PAYMENT_SERVICE_TELEGRAM_BOT_API_KEY
"

for key in $NEW_GENERATED_SECRETS; do
    ensure_generated_secret "$key"
done

MNEMOS_PRODUCTION_ENV_FILE="$WORKING_ENV" sh "$VALIDATOR"

backup_path="${TARGET_ENV}.backup-$(date -u +%Y%m%dT%H%M%SZ)"
install -o root -g root -m 0600 "$TARGET_ENV" "$backup_path"
install -o root -g root -m 0600 "$WORKING_ENV" "$TARGET_ENV"

cat <<EOF
Production environment migrated successfully:
  $TARGET_ENV

Previous environment backup:
  $backup_path

New internal credentials were generated locally and were not printed.
The Telegram bot enablement state was copied from NOTIFICATION_TELEGRAM_ENABLED when that legacy variable existed.
Legacy variables were intentionally preserved so the currently running production revision can still restart safely before cutover.
EOF
