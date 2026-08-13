SHELL := /bin/bash
.DEFAULT_GOAL := help
.NOTPARALLEL:

MNEMOS_PROJECT := 1
MAKE_DIR := make

include $(MAKE_DIR)/config.mk
include $(MAKE_DIR)/ui.mk
include $(MAKE_DIR)/validation.mk
include $(MAKE_DIR)/project.mk
include $(MAKE_DIR)/compose.mk
include $(MAKE_DIR)/kafka.mk
include $(MAKE_DIR)/postgres.mk
include $(MAKE_DIR)/keycloak.mk
include $(MAKE_DIR)/completion.mk
