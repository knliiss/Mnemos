ARG REDIS_VERSION

FROM redis:${REDIS_VERSION}

COPY --chmod=0755 infrastructure/redis/redis-entrypoint.sh /usr/local/bin/mnemos-redis-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/mnemos-redis-entrypoint.sh"]
CMD ["redis-server"]
