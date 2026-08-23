#!/bin/sh

set -eu

POLICY_NAME="mnemos-personalization-badges"
ROOT_ALIAS="mnemos"
APPLICATION_ALIAS="personalization"
MINIO_ENDPOINT="http://minio:9000"

require_environment() {
    for variable_name in \
        MINIO_ROOT_USER \
        MINIO_ROOT_PASSWORD \
        MINIO_PERSONALIZATION_BUCKET \
        MINIO_PERSONALIZATION_ACCESS_KEY \
        MINIO_PERSONALIZATION_SECRET_KEY; do
        eval "variable_value=\${${variable_name}:-}"

        if [ -z "$variable_value" ]; then
            echo "Required MinIO environment variable is missing: $variable_name" >&2
            exit 1
        fi
    done

    if [ "$MINIO_PERSONALIZATION_ACCESS_KEY" = "$MINIO_ROOT_USER" ]; then
        echo "MinIO application access key must differ from the root user" >&2
        exit 1
    fi
}

configure_root_alias() {
    mc alias set \
        "$ROOT_ALIAS" \
        "$MINIO_ENDPOINT" \
        "$MINIO_ROOT_USER" \
        "$MINIO_ROOT_PASSWORD" \
        >/dev/null
}

ensure_bucket() {
    mc mb --ignore-existing "$ROOT_ALIAS/$MINIO_PERSONALIZATION_BUCKET" >/dev/null
}

write_application_policy() {
    cat > /tmp/personalization-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket",
        "s3:GetBucketLocation"
      ],
      "Resource": [
        "arn:aws:s3:::$MINIO_PERSONALIZATION_BUCKET"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": [
        "arn:aws:s3:::$MINIO_PERSONALIZATION_BUCKET/badges/*",
        "arn:aws:s3:::$MINIO_PERSONALIZATION_BUCKET/personalizations/*"
      ]
    }
  ]
}
EOF

    mc admin policy create \
        "$ROOT_ALIAS" \
        "$POLICY_NAME" \
        /tmp/personalization-policy.json \
        >/dev/null
}

application_credentials_work() {
    mc alias set \
        "$APPLICATION_ALIAS" \
        "$MINIO_ENDPOINT" \
        "$MINIO_PERSONALIZATION_ACCESS_KEY" \
        "$MINIO_PERSONALIZATION_SECRET_KEY" \
        >/dev/null

    mc ls "$APPLICATION_ALIAS/$MINIO_PERSONALIZATION_BUCKET" >/dev/null 2>&1
}

attach_application_policy() {
    mc admin policy attach \
        "$ROOT_ALIAS" \
        "$POLICY_NAME" \
        --user "$MINIO_PERSONALIZATION_ACCESS_KEY" \
        >/dev/null
}

ensure_application_user() {
    if mc admin user info "$ROOT_ALIAS" "$MINIO_PERSONALIZATION_ACCESS_KEY" >/dev/null 2>&1; then
        attach_application_policy || true

        if application_credentials_work; then
            return
        fi

        mc admin user rm "$ROOT_ALIAS" "$MINIO_PERSONALIZATION_ACCESS_KEY" >/dev/null
    fi

    mc admin user add \
        "$ROOT_ALIAS" \
        "$MINIO_PERSONALIZATION_ACCESS_KEY" \
        "$MINIO_PERSONALIZATION_SECRET_KEY" \
        >/dev/null

    attach_application_policy
}

verify_application_access() {
    if ! application_credentials_work; then
        echo "MinIO application credentials cannot access the personalization bucket" >&2
        exit 1
    fi
}

require_environment
configure_root_alias
ensure_bucket
write_application_policy
ensure_application_user
verify_application_access

echo "MinIO personalization storage is ready"
