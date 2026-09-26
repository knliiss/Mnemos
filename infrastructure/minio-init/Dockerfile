ARG MINIO_MC_VERSION=RELEASE.2025-08-13T08-35-41Z

FROM alpine:3.22

ARG MINIO_MC_VERSION
ARG TARGETARCH

RUN apk add --no-cache ca-certificates curl \
    && case "${TARGETARCH:-amd64}" in \
        amd64) mc_arch="linux-amd64" ;; \
        arm64) mc_arch="linux-arm64" ;; \
        *) echo "Unsupported architecture: ${TARGETARCH}" >&2; exit 1 ;; \
    esac \
    && curl -fsSL \
        "https://dl.min.io/client/mc/release/${mc_arch}/archive/mc.${MINIO_MC_VERSION}" \
        -o /usr/local/bin/mc \
    && chmod +x /usr/local/bin/mc

COPY infrastructure/minio-init/init.sh /usr/local/bin/minio-init

RUN chmod +x /usr/local/bin/minio-init

ENTRYPOINT ["/usr/local/bin/minio-init"]
