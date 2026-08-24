#!/usr/bin/env sh
set -eu

TARGET_ENV="${1:-.env}"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Target env file does not exist: $TARGET_ENV" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required" >&2
    exit 1
fi

get_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$TARGET_ENV"
}

find_service_container() {
    project_name="$1"
    service="$2"

    docker ps -aq \
        --filter "label=com.docker.compose.project=$project_name" \
        --filter "label=com.docker.compose.service=$service" |
        head -n 1
}

require_value() {
    name="$1"
    value="$2"

    if [ -z "$value" ]; then
        echo "$name is required" >&2
        exit 1
    fi
}

project_name="$(get_env "COMPOSE_PROJECT_NAME")"
root_user="$(get_env "MINIO_ROOT_USER")"
root_password="$(get_env "MINIO_ROOT_PASSWORD")"
application_access_key="$(get_env "MINIO_PERSONALIZATION_ACCESS_KEY")"
application_secret_key="$(get_env "MINIO_PERSONALIZATION_SECRET_KEY")"
application_bucket="$(get_env "MINIO_PERSONALIZATION_BUCKET")"

require_value "COMPOSE_PROJECT_NAME" "$project_name"
require_value "MINIO_ROOT_USER" "$root_user"
require_value "MINIO_ROOT_PASSWORD" "$root_password"
require_value "MINIO_PERSONALIZATION_ACCESS_KEY" "$application_access_key"
require_value "MINIO_PERSONALIZATION_SECRET_KEY" "$application_secret_key"
require_value "MINIO_PERSONALIZATION_BUCKET" "$application_bucket"

minio_container_id="$(find_service_container "$project_name" "minio")"
minio_init_container_id="$(find_service_container "$project_name" "minio-init")"

require_value "MinIO container id" "$minio_container_id"
require_value "MinIO init container id" "$minio_init_container_id"

minio_state="$(docker inspect --format '{{.State.Status}}' "$minio_container_id")"
minio_init_state="$(docker inspect --format '{{.State.Status}}' "$minio_init_container_id")"
minio_init_image="$(docker inspect --format '{{.Config.Image}}' "$minio_init_container_id")"

if [ "$minio_state" != "running" ]; then
    echo "MinIO must be running for policy reconciliation verification" >&2
    exit 1
fi

if [ "$minio_init_state" != "exited" ]; then
    echo "MinIO init must have completed before policy reconciliation verification" >&2
    exit 1
fi

require_value "MinIO init image" "$minio_init_image"

run_root_mc() {
    docker run --rm \
        --network "container:$minio_container_id" \
        --entrypoint /bin/sh \
        -e "MNEMOS_TEST_ROOT_USER=$root_user" \
        -e "MNEMOS_TEST_ROOT_PASSWORD=$root_password" \
        "$minio_init_image" \
        -c '
            set -eu
            mc alias set root http://127.0.0.1:9000 "$MNEMOS_TEST_ROOT_USER" "$MNEMOS_TEST_ROOT_PASSWORD" >/dev/null
            shift
            mc "$@"
        ' \
        sh \
        "$@"
}

run_application_mc() {
    docker run --rm \
        --network "container:$minio_container_id" \
        --entrypoint /bin/sh \
        -e "MNEMOS_TEST_ACCESS_KEY=$application_access_key" \
        -e "MNEMOS_TEST_SECRET_KEY=$application_secret_key" \
        "$minio_init_image" \
        -c '
            set -eu
            mc alias set application http://127.0.0.1:9000 "$MNEMOS_TEST_ACCESS_KEY" "$MNEMOS_TEST_SECRET_KEY" >/dev/null
            shift
            mc "$@"
        ' \
        sh \
        "$@"
}

probe_bucket="mnemos-minio-policy-probe"

cleanup() {
    run_root_mc admin policy detach root readwrite --user "$application_access_key" >/dev/null 2>&1 || true
    run_root_mc rb --force "root/$probe_bucket" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

run_root_mc mb --ignore-existing "root/$probe_bucket" >/dev/null
run_root_mc admin policy attach root readwrite --user "$application_access_key" >/dev/null

if ! run_application_mc ls "application/$probe_bucket" >/dev/null 2>&1; then
    echo "MinIO stale-policy probe could not establish broad application access" >&2
    exit 1
fi

docker start -a "$minio_init_container_id" >/dev/null

if run_application_mc ls "application/$probe_bucket" >/dev/null 2>&1; then
    echo "MinIO init left a stale broad application policy attached" >&2
    exit 1
fi

if ! run_application_mc ls "application/$application_bucket" >/dev/null 2>&1; then
    echo "MinIO init removed required personalization bucket access" >&2
    exit 1
fi

echo "MinIO application policy reconciliation verified"
