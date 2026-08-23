#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
EXPECTED_AUDIENCE="mnemos-api"

fail() {
    echo "$1" >&2
    exit 1
}

verified_services=0

for service_dir in "$REPOSITORY_ROOT"/apps/*-service; do
    [ -d "$service_dir" ] || continue

    properties_file="$service_dir/src/main/resources/application.properties"

    if [ ! -f "$properties_file" ]; then
        continue
    fi

    if ! grep -Fq 'spring.security.oauth2.resourceserver.jwt.issuer-uri=' "$properties_file"; then
        continue
    fi

    grep -Fqx "spring.security.oauth2.resourceserver.jwt.audiences[0]=$EXPECTED_AUDIENCE" "$properties_file" ||
        fail "$(basename "$service_dir") does not require JWT audience $EXPECTED_AUDIENCE"

    verified_services=$((verified_services + 1))
done

GATEWAY_CONFIG="$REPOSITORY_ROOT/apps/gateway-service/src/main/resources/application.yml"
KEYCLOAK_CLIENTS="$REPOSITORY_ROOT/infrastructure/keycloak/config/40-clients.yaml"

grep -Fq '          audiences:' "$GATEWAY_CONFIG" ||
    fail "gateway-service does not configure resource-server audiences"
grep -Fq "            - $EXPECTED_AUDIENCE" "$GATEWAY_CONFIG" ||
    fail "gateway-service does not require JWT audience $EXPECTED_AUDIENCE"

grep -Fq '      - name: mnemos-api-audience' "$KEYCLOAK_CLIENTS" ||
    fail "Keycloak Mini App client is missing the API audience mapper"
grep -Fq "          included.custom.audience: $EXPECTED_AUDIENCE" "$KEYCLOAK_CLIENTS" ||
    fail "Keycloak Mini App client does not emit audience $EXPECTED_AUDIENCE"

echo "JWT audience configuration verified: $verified_services servlet services plus gateway"
