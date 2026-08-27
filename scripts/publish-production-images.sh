#!/bin/sh

set -eu

REGISTRY="${MNEMOS_IMAGE_REGISTRY:?MNEMOS_IMAGE_REGISTRY is required}"
TAG="${MNEMOS_IMAGE_TAG:?MNEMOS_IMAGE_TAG is required}"
PUSH="${MNEMOS_IMAGE_PUSH:-true}"

if [ "$PUSH" = "true" ]; then
    OUTPUT_ARGS="--push"
elif [ "$PUSH" = "false" ]; then
    OUTPUT_ARGS="--load"
else
    echo "MNEMOS_IMAGE_PUSH must be true or false" >&2
    exit 1
fi

build_image() {
    image_name="$1"
    dockerfile="$2"
    shift 2

    image="${REGISTRY}/${image_name}:${TAG}"

    echo "Building ${image}"

    # shellcheck disable=SC2086
    docker buildx build \
        --file "$dockerfile" \
        --tag "$image" \
        $OUTPUT_ARGS \
        "$@" \
        .
}

build_image "mnemos-account-service" "apps/account-service/Dockerfile"
build_image "mnemos-bet-service" "apps/bet-service/Dockerfile"
build_image "mnemos-event-service" "apps/event-service/Dockerfile"
build_image "mnemos-gateway-service" "apps/gateway-service/Dockerfile"
build_image "mnemos-moderation-service" "apps/moderation-service/Dockerfile"
build_image "mnemos-notification-service" "apps/notification-service/Dockerfile"
build_image "mnemos-payment-service" "apps/payment-service/Dockerfile"
build_image "mnemos-personalization-service" "apps/personalization-service/Dockerfile"
build_image "mnemos-player-directory-service" "apps/player-directory-service/Dockerfile"
build_image "mnemos-profile-service" "apps/profile-service/Dockerfile"
build_image "mnemos-realtime-service" "apps/realtime-service/Dockerfile"
build_image "mnemos-reward-service" "apps/reward-service/Dockerfile"
build_image "mnemos-social-service" "apps/social-service/Dockerfile"
build_image "mnemos-wallet-service" "apps/wallet-service/Dockerfile"

build_image \
    "mnemos-keycloak" \
    "infrastructure/keycloak/Dockerfile" \
    --build-arg "KEYCLOAK_VERSION=${KEYCLOAK_VERSION:?KEYCLOAK_VERSION is required}"
