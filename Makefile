SHELL := /bin/bash
.DEFAULT_GOAL := help
.NOTPARALLEL:

MAKE_DIR := make

# Target declarations are intentionally kept in the root Makefile.
# Shell and IDE completion usually scan only this file and may ignore includes.

help:
status:
ps:
urls:
logs-errors:

check-tools:
check-env:
validate:
doctor:
config:
config-infra:
config-account:
config-keycloak:

build:
check:
test:
clean:
dependencies:

up:
stop:
down:
restart:
rebuild:
pull:
logs:
reset:

infra-up:
infra-stop:
infra-down:
infra-restart:
infra-logs:

postgres-up:
postgres-stop:
postgres-restart:
postgres-logs:
postgres-status:
postgres-psql-app:
postgres-psql-keycloak:

kafka-up:
kafka-stop:
kafka-restart:
kafka-logs:
kafka-topics:
kafka-groups:
kafka-topic-create:
kafka-topic-describe:
kafka-topic-delete:
kafka-api-versions:

telegram-auth-clean:
telegram-auth-build:
telegram-auth-verify:
telegram-auth-info:

keycloak-up:
keycloak-stop:
keycloak-restart:
keycloak-logs:
keycloak-errors:
keycloak-config:
keycloak-provider-status:

account-jar:
account-image:
account-build:
account-rebuild-image:
account-up:
account-stop:
account-down:
account-restart:
account-logs:
account-errors:
account-config:
account-shell:
account-health:

include $(MAKE_DIR)/config.mk
include $(MAKE_DIR)/ui.mk
include $(MAKE_DIR)/validation.mk
include $(MAKE_DIR)/diagnostics.mk
include $(MAKE_DIR)/project.mk
include $(MAKE_DIR)/stack.mk
include $(MAKE_DIR)/infra.mk
include $(MAKE_DIR)/postgres.mk
include $(MAKE_DIR)/kafka.mk
include $(MAKE_DIR)/keycloak.mk
include $(MAKE_DIR)/account.mk