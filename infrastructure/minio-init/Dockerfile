ARG MINIO_MC_VERSION=RELEASE.2025-08-13T08-35-41Z

FROM alpine:3.22

ARG MINIO_MC_VERSION
ARG TARGETARCH

RUN apk add --no-cache ca-certificates curl \
    && case "${TARGETARCH:-amd64}" in \
        amd64) \
            mc_arch="amd64"; \
            mc_sha256="01f866e9c5f9b87c2b09116fa5d7c06695b106242d829a8bb32990c00312e891" \
            ;; \
        arm64) \
            mc_arch="arm64"; \
            mc_sha256="14c8c9616cfce4636add161304353244e8de383b2e2752c0e9dad01d4c27c12c" \
            ;; \
        *) \
            echo "Unsupported architecture: ${TARGETARCH}" >&2; \
            exit 1 \
            ;; \
    esac \
    && curl -fsSL \
        "https://github.com/minio/mc/releases/download/${MINIO_MC_VERSION}/mc.linux-${mc_arch}.${MINIO_MC_VERSION}" \
        -o /usr/local/bin/mc \
    && echo "${mc_sha256}  /usr/local/bin/mc" | sha256sum -c - \
    && chmod +x /usr/local/bin/mc

COPY infrastructure/minio-init/init.sh /usr/local/bin/minio-init

RUN chmod +x /usr/local/bin/minio-init

ENTRYPOINT ["/usr/local/bin/minio-init"]
