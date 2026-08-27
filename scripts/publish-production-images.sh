#!/bin/sh

set -eu

REGISTRY="${MNEMOS_IMAGE_REGISTRY:?MNEMOS_IMAGE_REGISTRY is required}"
TAG="${MNEMOS_IMAGE_TAG:?MNEMOS_IMAGE_TAG is required}"
PUSH="${MNEMOS_IMAGE_PUSH:-true}"

case "$PUSH" in
    true | false)
        ;;
    *)
        echo "MNEMOS_IMAGE_PUSH must be true or false" >&2
        exit 1
        ;;
esac

build_image() {
    image_name="$1"
    dockerfile="$2"
    context="$3"
    shift 3

    image="${REGISTRY}/${image_name}:${TAG}"

    echo "Building ${image}"

    if [ "$PUSH" = "true" ]; then
        docker buildx build \
            --file "$dockerfile" \
            --tag "$image" \
            --push \
            "$@" \
            "$context"
    else
        docker buildx build \
            --file "$dockerfile" \
            --tag "$image" \
            --load \
            "$@" \
            "$context"
    fi
}

build_image "mnemos-account-service" "apps/account-service/Dockerfile" "."
build_image "mnemos-bet-service" "apps/bet-service/Dockerfile" "."
build_image "mnemos-event-service" "apps/event-service/Dockerfile" "."
build_image "mnemos-gateway-service" "apps/gateway-service/Dockerfile" "."
build_image "mnemos-moderation-service" "apps/moderation-service/Dockerfile" "."
build_image "mnemos-notification-service" "apps/notification-service/Dockerfile" "."
build_image "mnemos-payment-service" "apps/payment-service/Dockerfile" "."
build_image "mnemos-personalization-service" "apps/personalization-service/Dockerfile" "."
build_image "mnemos-player-directory-service" "apps/player-directory-service/Dockerfile" "."
build_image "mnemos-profile-service" "apps/profile-service/Dockerfile" "."
build_image "mnemos-realtime-service" "apps/realtime-service/Dockerfile" "."
build_image "mnemos-reward-service" "apps/reward-service/Dockerfile" "."
build_image "mnemos-social-service" "apps/social-service/Dockerfile" "."
build_image "mnemos-wallet-service" "apps/wallet-service/Dockerfile" "."

build_image \
    "mnemos-keycloak" \
    "infrastructure/keycloak/Dockerfile" \
    "." \
    --build-arg "KEYCLOAK_VERSION=${KEYCLOAK_VERSION:?KEYCLOAK_VERSION is required}"

build_image \
    "mnemos-redis" \
    "infrastructure/redis/Dockerfile" \
    "." \
    --build-arg "REDIS_VERSION=${REDIS_VERSION:?REDIS_VERSION is required}"

build_image \
    "mnemos-kafka-tls-init" \
    "infrastructure/kafka/Dockerfile.tls-init" \
    "." \
    --build-arg "KAFKA_VERSION=${KAFKA_VERSION:?KAFKA_VERSION is required}"

build_image \
    "mnemos-kafka-init" \
    "Dockerfile.init" \
    "infrastructure/kafka" \
    --build-arg "KAFKA_VERSION=${KAFKA_VERSION:?KAFKA_VERSION is required}"

build_image \
    "mnemos-minio-init" \
    "infrastructure/minio-init/Dockerfile" \
    "." \
    --build-arg "MINIO_MC_VERSION=${MINIO_MC_VERSION:?MINIO_MC_VERSION is required}"
