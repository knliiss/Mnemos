#!/bin/sh

set -eu

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
REGISTRY="${MNEMOS_IMAGE_REGISTRY:?MNEMOS_IMAGE_REGISTRY is required}"
TAG="${MNEMOS_IMAGE_TAG:?MNEMOS_IMAGE_TAG is required}"
PUSH="${MNEMOS_IMAGE_PUSH:-true}"
DRY_RUN="${MNEMOS_IMAGE_DRY_RUN:-false}"

for boolean_value in "$PUSH" "$DRY_RUN"; do
    case "$boolean_value" in
        true | false)
            ;;
        *)
            echo "MNEMOS_IMAGE_PUSH and MNEMOS_IMAGE_DRY_RUN must be true or false" >&2
            exit 1
            ;;
    esac
done

SERVICES="$(
    python3 "$REPOSITORY_ROOT/scripts/production_service_catalog.py" list
)"

if [ -z "$SERVICES" ]; then
    echo "Production service catalog is empty" >&2
    exit 1
fi

for service in $SERVICES; do
    set -- \
        --service "$service" \
        --registry "$REGISTRY" \
        --tag "$TAG" \
        --skip-assemble

    if [ "$PUSH" = "true" ]; then
        set -- "$@" --push
    else
        set -- "$@" --load
    fi

    if [ "$DRY_RUN" = "true" ]; then
        set -- "$@" --dry-run
    fi

    python3 \
        "$REPOSITORY_ROOT/scripts/publish-production-service-image.py" \
        "$@"
done
