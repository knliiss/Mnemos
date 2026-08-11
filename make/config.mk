# =============================================================================
# Shared configuration
# =============================================================================

PROJECT_NAME ?= mnemos
ENV_FILE ?= .env
GRADLEW ?= ./gradlew

LOG_TAIL ?= 150
WAIT_TIMEOUT ?= 180

COMPOSE_DIR := infrastructure/compose
APPS_COMPOSE_DIR := $(COMPOSE_DIR)/apps

BASE_FILE := $(COMPOSE_DIR)/compose.base.yml
POSTGRES_FILE := $(COMPOSE_DIR)/compose.postgres.yml
KAFKA_FILE := $(COMPOSE_DIR)/compose.kafka.yml
REDIS_FILE := $(COMPOSE_DIR)/compose.redis.yml
KEYCLOAK_FILE := $(COMPOSE_DIR)/compose.keycloak.yml
KEYCLOAK_CONFIG_FILE := $(COMPOSE_DIR)/compose.keycloak-config.yml
ACCOUNT_SERVICE_FILE := $(APPS_COMPOSE_DIR)/compose.account-service.yml
WALLET_SERVICE_FILE := $(APPS_COMPOSE_DIR)/compose.wallet-service.yml
EVENT_SERVICE_FILE := $(APPS_COMPOSE_DIR)/compose.event-service.yml
NOTIFICATION_SERVICE_FILE := $(APPS_COMPOSE_DIR)/compose.notification-service.yml

DC := docker compose --env-file $(ENV_FILE)
DC_BASE := $(DC) -f $(BASE_FILE)
DC_POSTGRES := $(DC_BASE) -f $(POSTGRES_FILE)
DC_AUTH := $(DC_POSTGRES) -f $(KEYCLOAK_FILE)
DC_KEYCLOAK_CONFIG := $(DC_AUTH) -f $(KEYCLOAK_CONFIG_FILE)
DC_REDIS := $(DC_BASE) -f $(REDIS_FILE)
DC_INFRA := $(DC_AUTH) -f $(KAFKA_FILE) -f $(REDIS_FILE)

# account-service always uses the complete infrastructure set, including Kafka.
DC_ACCOUNT := $(DC_INFRA) -f $(ACCOUNT_SERVICE_FILE)
DC_WALLET := $(DC_INFRA) -f $(WALLET_SERVICE_FILE)
DC_EVENT := $(DC_INFRA) -f $(EVENT_SERVICE_FILE)
DC_NOTIFICATION := $(DC_INFRA) -f $(NOTIFICATION_SERVICE_FILE)
DC_ALL := $(DC_INFRA) -f $(ACCOUNT_SERVICE_FILE) -f $(WALLET_SERVICE_FILE) -f $(EVENT_SERVICE_FILE) -f $(NOTIFICATION_SERVICE_FILE)

APP_POSTGRES_SERVICE := app-postgres
KEYCLOAK_POSTGRES_SERVICE := keycloak-postgres
KAFKA_SERVICE := kafka
KEYCLOAK_SERVICE := keycloak
KEYCLOAK_CONFIG_SERVICE := keycloak-config
ACCOUNT_SERVICE := account-service
WALLET_SERVICE := wallet-service
EVENT_SERVICE := event-service
NOTIFICATION_SERVICE := notification-service

ACCOUNT_MODULE := :apps:account-service
WALLET_MODULE := :apps:wallet-service
EVENT_MODULE := :apps:event-service
NOTIFICATION_MODULE := :apps:notification-service
TELEGRAM_AUTH_MODULE := :keycloak-extensions:telegram-authenticator
TELEGRAM_AUTH_JAR := keycloak-extensions/telegram-authenticator/build/libs/mnemos-telegram-authenticator.jar
TELEGRAM_AUTH_SERVICE_FILE := META-INF/services/org.keycloak.authentication.AuthenticatorFactory
KEYCLOAK_PROVIDER_PATH := /opt/keycloak/providers/mnemos-telegram-authenticator.jar

KAFKA_BOOTSTRAP_INTERNAL ?= kafka:29092
KAFKA_TOOLS_DIR ?= /opt/kafka/bin

TOPIC ?=
PARTITIONS ?= 3
REPLICATION_FACTOR ?= 1
