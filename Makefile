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
config-wallet:
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

redis-up:
redis-stop:
redis-restart:
redis-logs:
redis-status:
redis-cli:

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

wallet-jar:
wallet-image:
wallet-build:
wallet-rebuild-image:
wallet-up:
wallet-stop:
wallet-down:
wallet-restart:
wallet-logs:
wallet-errors:
wallet-config:
wallet-shell:
wallet-health:

event-jar:
event-image:
event-build:
event-rebuild-image:
event-up:
event-stop:
event-down:
event-restart:
event-logs:
event-errors:
event-config:
event-shell:
event-health:

notification-jar:
notification-image:
notification-build:
notification-rebuild-image:
notification-up:
notification-stop:
notification-down:
notification-restart:
notification-logs:
notification-errors:
notification-config:
notification-shell:
notification-health:

include $(MAKE_DIR)/config.mk
include $(MAKE_DIR)/ui.mk
include $(MAKE_DIR)/validation.mk
include $(MAKE_DIR)/diagnostics.mk
include $(MAKE_DIR)/project.mk
include $(MAKE_DIR)/stack.mk
include $(MAKE_DIR)/infra.mk
include $(MAKE_DIR)/postgres.mk
include $(MAKE_DIR)/kafka.mk
include $(MAKE_DIR)/redis.mk
include $(MAKE_DIR)/keycloak.mk
include $(MAKE_DIR)/account.mk
include $(MAKE_DIR)/wallet.mk
include $(MAKE_DIR)/event.mk
include $(MAKE_DIR)/notification.mk