#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT INT TERM

ENV_FILE="$TEMP_DIR/test.env"
DOCKER_STUB="$TEMP_DIR/docker"

cat > "$ENV_FILE" <<'EOF'
MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS=account-service=file-public-key;wallet-service=file-public-key
ACCOUNT_SERVICE_KAFKA_SIGNING_PRIVATE_KEY=file-private-key
KAFKA_ACCOUNT_SERVICE_PASSWORD=file-password
EOF

cat > "$DOCKER_STUB" <<'EOF'
#!/usr/bin/env sh
set -eu

fail() {
    echo "$1" >&2
    exit 1
}

[ "${MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS+x}" != "x" ] ||
    fail "Compose wrapper leaked shell MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS"
[ "${ACCOUNT_SERVICE_KAFKA_SIGNING_PRIVATE_KEY+x}" != "x" ] ||
    fail "Compose wrapper leaked shell signing private key"
[ "${KAFKA_ACCOUNT_SERVICE_PASSWORD+x}" != "x" ] ||
    fail "Compose wrapper leaked shell Kafka password"
[ "${UNRELATED_SENTINEL:-}" = "preserved" ] ||
    fail "Compose wrapper removed an unrelated environment variable"

[ "${1:-}" = "compose" ] || fail "Compose wrapper did not invoke docker compose"
[ "${2:-}" = "--env-file" ] || fail "Compose wrapper did not pass --env-file"
[ "${3:-}" = "$EXPECTED_ENV_FILE" ] || fail "Compose wrapper passed the wrong env file"
[ "${4:-}" = "config" ] || fail "Compose wrapper did not preserve compose arguments"
[ "${5:-}" = "--quiet" ] || fail "Compose wrapper did not preserve compose arguments"
EOF
chmod +x "$DOCKER_STUB"

export MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS="account-service=stale-shell-key"
export ACCOUNT_SERVICE_KAFKA_SIGNING_PRIVATE_KEY="stale-shell-private-key"
export KAFKA_ACCOUNT_SERVICE_PASSWORD="stale-shell-password"
export UNRELATED_SENTINEL="preserved"
export EXPECTED_ENV_FILE="$ENV_FILE"

PATH="$TEMP_DIR:$PATH" \
    "$SCRIPT_DIR/docker-compose-env.sh" "$ENV_FILE" config --quiet

echo "Compose Kafka environment isolation verified"
