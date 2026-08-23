ARG MINIO_MC_VERSION=RELEASE.2025-08-13T08-35-41Z

FROM quay.io/minio/mc:${MINIO_MC_VERSION} AS minio-client

FROM alpine:3.22

RUN apk add --no-cache ca-certificates

COPY --from=minio-client /usr/bin/mc /usr/local/bin/mc
COPY infrastructure/minio-init/init.sh /usr/local/bin/minio-init

RUN chmod +x /usr/local/bin/minio-init

ENTRYPOINT ["/usr/local/bin/minio-init"]
