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
APP_BUILD_TASKS := $(foreach service,$(APP_SERVICE_NAMES),:apps:$(service):build)
APP_TEST_TASKS := $(foreach service,$(APP_SERVICE_NAMES),:apps:$(service):test)
APP_CHECK_TASKS := $(foreach service,$(APP_SERVICE_NAMES),:apps:$(service):check)
APP_FORMAT_TASKS := $(foreach service,$(APP_SERVICE_NAMES),:apps:$(service):ktlintFormat)
APP_CLEAN_TASKS := $(foreach service,$(APP_SERVICE_NAMES),:apps:$(service):clean)

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

BUILDABLE_INFRA_SERVICE_NAMES := kafka-init
BUILDABLE_SERVICE_NAMES := $(sort $(APP_SERVICE_NAMES) $(BUILDABLE_INFRA_SERVICE_NAMES))
ONE_SHOT_SERVICE_NAMES := kafka-init keycloak-config

LOG_TAIL ?= 100
FAILURE_LOG_TAIL ?= 80
STARTUP_CHECK_DELAY ?= 5
KAFKA_PARTITIONS ?= 3
KAFKA_REPLICATION_FACTOR ?= 1
