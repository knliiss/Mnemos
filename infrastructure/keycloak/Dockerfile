ARG KEYCLOAK_VERSION=26.7.0

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder

ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

COPY keycloak-extensions/telegram-authenticator/build/libs/mnemos-telegram-authenticator.jar /opt/keycloak/providers/mnemos-telegram-authenticator.jar
COPY infrastructure/keycloak/themes/mnemos /opt/keycloak/themes/mnemos

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}

COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
