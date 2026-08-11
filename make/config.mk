ENV_FILE ?= .env
GRADLEW ?= ./gradlew
COMPOSE_DIR := infrastructure/compose
BASE_COMPOSE := $(COMPOSE_DIR)/compose.base.yml
INFRA_COMPOSE := $(sort $(filter-out $(BASE_COMPOSE),$(wildcard $(COMPOSE_DIR)/compose.*.yml)))
APP_COMPOSE := $(sort $(wildcard $(COMPOSE_DIR)/apps/compose.*.yml))
COMPOSE_FILES := $(BASE_COMPOSE) $(INFRA_COMPOSE) $(APP_COMPOSE)
COMPOSE_ARGS := $(foreach file,$(COMPOSE_FILES),-f $(file))
DC := docker compose --env-file $(ENV_FILE) $(COMPOSE_ARGS)
LOG_TAIL ?= 150
SERVICE ?=
TARGET ?=
MODULE ?=
TOPIC ?=
PARTITIONS ?= 3
REPLICATION_FACTOR ?= 1

# Accept both SERVICE=reward and SERVICE=reward-service.
SERVICE_NAME = $(if $(SERVICE),$(if $(filter %-service,$(SERVICE)),$(SERVICE),$(SERVICE)-service),)
SERVICE_SHORT = $(patsubst %-service,%,$(SERVICE_NAME))
APP_MODULE = :apps:$(SERVICE_NAME)
CONTAINER_TARGET = $(if $(TARGET),$(TARGET),$(SERVICE_NAME))
