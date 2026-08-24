#!/bin/sh
set -eu

require_secret() {
    name="$1"
    value="$2"

    if [ -z "$value" ]; then
        echo "$name is required" >&2
        exit 1
    fi
}

password_hash() {
    printf '%s' "$1" | sha256sum | awk '{print $1}'
}

require_secret "REDIS_ADMIN_PASSWORD" "${REDIS_ADMIN_PASSWORD:-}"
require_secret "REDIS_GATEWAY_PASSWORD" "${REDIS_GATEWAY_PASSWORD:-}"
require_secret "REDIS_REALTIME_PASSWORD" "${REDIS_REALTIME_PASSWORD:-}"
require_secret "REDIS_NOTIFICATION_PASSWORD" "${REDIS_NOTIFICATION_PASSWORD:-}"

admin_password_hash="$(password_hash "$REDIS_ADMIN_PASSWORD")"
gateway_password_hash="$(password_hash "$REDIS_GATEWAY_PASSWORD")"
realtime_password_hash="$(password_hash "$REDIS_REALTIME_PASSWORD")"
notification_password_hash="$(password_hash "$REDIS_NOTIFICATION_PASSWORD")"

acl_directory=/run/mnemos-redis
acl_file="$acl_directory/users.acl"

mkdir -p "$acl_directory"

cat > "$acl_file" <<EOF
user default off
user redis_admin on #$admin_password_hash ~* &* +@all
user gateway_service on #$gateway_password_hash resetkeys ~mnemos:gateway:* resetchannels -@all +@read +@write +@transaction +eval +evalsha +ping +echo +hello +auth +select +command +quit +time +client|setname +client|setinfo +client|getname +client|id -keys -scan -randomkey -dbsize -info -migrate -sort -sort_ro -flushall -flushdb -swapdb -function -script
user realtime_service on #$realtime_password_hash resetkeys ~realtime:* resetchannels -@all +@read +@write +@transaction +eval +evalsha +ping +echo +hello +auth +select +command +quit +time +client|setname +client|setinfo +client|getname +client|id -keys -scan -randomkey -dbsize -info -migrate -sort -sort_ro -flushall -flushdb -swapdb -function -script
user notification_service on #$notification_password_hash resetkeys ~notification:* resetchannels -@all +@read +@write +@transaction +eval +evalsha +ping +echo +hello +auth +select +command +quit +time +client|setname +client|setinfo +client|getname +client|id -keys -scan -randomkey -dbsize -info -migrate -sort -sort_ro -flushall -flushdb -swapdb -function -script
EOF

chown redis:redis "$acl_directory" "$acl_file"
chmod 0700 "$acl_directory"
chmod 0600 "$acl_file"

exec /usr/local/bin/docker-entrypoint.sh "$@"
