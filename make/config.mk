ENV_FILE ?= .env
GRADLEW ?= ./gradlew

COMPOSE_DIR := infrastructure/compose
BASE_COMPOSE := $(COMPOSE_DIR)/compose.base.yml
INFRA_COMPOSE := $(sort $(filter-out $(BASE_COMPOSE),$(wildcard $(COMPOSE_DIR)/compose.*.yml)))
APP_COMPOSE := $(sort $(wildcard $(COMPOSE_DIR)/apps/compose.*.yml))
COMPOSE_FILES := $(BASE_COMPOSE) $(INFRA_COMPOSE) $(APP_COMPOSE)
COMPOSE_ARGS := $(foreach file,$(COMPOSE_FILES),-f $(file))
DC := docker compose --env-file $(ENV_FILE) $(COMPOSE_ARGS)

APP_SERVICE_NAMES := $(sort $(notdir $(patsubst %/,%,$(wildcard apps/*-service/))))
APP_NAMES := $(patsubst %-service,%,$(APP_SERVICE_NAMES))

COMPOSE_SERVICE_NAMES := $(sort $(shell \
	awk '\
		/^services:[[:space:]]*$$/ { in_services = 1; next } \
		in_services && /^[^[:space:]]/ { in_services = 0 } \
		in_services && /^  [A-Za-z0-9_-]+:[[:space:]]*$$/ { \
			line = $$0; \
			sub(/^  /, "", line); \
			sub(/:.*/, "", line); \
			print line; \
		}' $(COMPOSE_FILES) 2>/dev/null))

INFRA_SERVICE_NAMES := $(filter-out $(APP_SERVICE_NAMES),$(COMPOSE_SERVICE_NAMES))

LOG_TAIL ?= 100
KAFKA_PARTITIONS ?= 3
KAFKA_REPLICATION_FACTOR ?= 1
