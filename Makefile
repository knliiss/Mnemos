SHELL := /bin/bash
.DEFAULT_GOAL := help

# =============================================================================
# Mnemos — development commands
# =============================================================================

PROJECT_NAME ?= mnemos
ENV_FILE ?= .env
GRADLEW ?= ./gradlew

COMPOSE_DIR := infrastructure/compose
APPS_COMPOSE_DIR := $(COMPOSE_DIR)/apps

BASE_FILE := $(COMPOSE_DIR)/compose.base.yml
POSTGRES_FILE := $(COMPOSE_DIR)/compose.postgres.yml
KAFKA_FILE := $(COMPOSE_DIR)/compose.kafka.yml
KEYCLOAK_FILE := $(COMPOSE_DIR)/compose.keycloak.yml
KEYCLOAK_CONFIG_FILE := $(COMPOSE_DIR)/compose.keycloak-config.yml
ACCOUNT_SERVICE_FILE := $(APPS_COMPOSE_DIR)/compose.account-service.yml

DC := docker compose --env-file $(ENV_FILE)
DC_BASE := $(DC) -f $(BASE_FILE)
DC_POSTGRES := $(DC_BASE) -f $(POSTGRES_FILE)
DC_AUTH := $(DC_POSTGRES) -f $(KEYCLOAK_FILE)
DC_INFRA := $(DC_AUTH) -f $(KAFKA_FILE)
DC_KEYCLOAK_CONFIG := $(DC_AUTH) -f $(KEYCLOAK_CONFIG_FILE)
DC_ACCOUNT := $(DC_AUTH) -f $(ACCOUNT_SERVICE_FILE)
DC_ALL := $(DC_INFRA) -f $(ACCOUNT_SERVICE_FILE)

ACCOUNT_SERVICE := account-service
ACCOUNT_MODULE := :apps:account-service
TELEGRAM_AUTH_MODULE := :keycloak-extensions:telegram-authenticator
KEYCLOAK_CONFIG_SERVICE := keycloak-config

# ANSI colors
RESET := \033[0m
BOLD := \033[1m
DIM := \033[2m
RED := \033[31m
GREEN := \033[32m
YELLOW := \033[33m
BLUE := \033[34m
MAGENTA := \033[35m
CYAN := \033[36m

BOX_WIDTH := 78

.PHONY: \
	help check-env validate config status ps urls \
	build test clean \
	up stop down restart logs \
	infra-up infra-stop infra-down infra-restart infra-logs \
	postgres-up postgres-stop postgres-logs \
	kafka-up kafka-stop kafka-logs \
	keycloak-up keycloak-stop keycloak-restart keycloak-logs keycloak-config keycloak-log-level \
	account-build account-up account-stop account-down account-restart account-logs account-config

# =============================================================================
##@ General
# =============================================================================

help: ## Show this help
	@printf "\n$(BOLD)$(CYAN)╭────────────────────────────────────────────────────────────────────────────╮$(RESET)\n"
	@printf "$(BOLD)$(CYAN)│$(RESET)  $(BOLD)Mnemos development toolkit$(RESET)                                                $(BOLD)$(CYAN)│$(RESET)\n"
	@printf "$(BOLD)$(CYAN)│$(RESET)  $(DIM)Infrastructure, applications, builds and diagnostics$(RESET)                      $(BOLD)$(CYAN)│$(RESET)\n"
	@printf "$(BOLD)$(CYAN)╰────────────────────────────────────────────────────────────────────────────╯$(RESET)\n\n"
	@awk '\
		BEGIN { FS = ":.*## " } \
		/^##@ / { \
			if (printed) printf "\n"; \
			printf "$(BOLD)$(MAGENTA)%s$(RESET)\n", substr($$0, 5); \
			printed = 1; \
			next \
		} \
		/^[a-zA-Z0-9_.-]+:.*## / { \
			printf "  $(GREEN)%-23s$(RESET) %s\n", $$1, $$2 \
		} \
	' $(MAKEFILE_LIST)
	@printf "\n$(DIM)Examples$(RESET)\n"
	@printf "  $(CYAN)make infra-up$(RESET)       start PostgreSQL, Keycloak and Kafka\n"
	@printf "  $(CYAN)make account-up$(RESET)     build and start account-service\n"
	@printf "  $(CYAN)make status$(RESET)         show the whole stack in one compact box\n"
	@printf "  $(CYAN)make account-logs$(RESET)   follow account-service logs\n\n"

check-env: ## Validate .env and required variables
	@set -eu; \
	if [ ! -f "$(ENV_FILE)" ]; then \
		printf "$(RED)✗ Environment file not found: $(ENV_FILE)$(RESET)\n"; \
		exit 1; \
	fi; \
	set -a; source "$(ENV_FILE)"; set +a; \
	missing=""; \
	for variable in \
		COMPOSE_PROJECT_NAME \
		POSTGRES_VERSION \
		APP_POSTGRES_DB \
		APP_POSTGRES_USER \
		APP_POSTGRES_PASSWORD \
		APP_POSTGRES_HOST_PORT \
		KEYCLOAK_POSTGRES_DB \
		KEYCLOAK_POSTGRES_USER \
		KEYCLOAK_POSTGRES_PASSWORD \
		KEYCLOAK_POSTGRES_HOST_PORT \
		KEYCLOAK_VERSION \
		KEYCLOAK_HOST_PORT \
		KEYCLOAK_ADMIN_USERNAME \
		KEYCLOAK_ADMIN_PASSWORD \
		KAFKA_VERSION \
		KAFKA_HOST_PORT; do \
		eval "value=\$${variable:-}"; \
		if [ -z "$$value" ]; then missing="$$missing $$variable"; fi; \
	done; \
	if [ -n "$$missing" ]; then \
		printf "$(RED)✗ Missing required variables in $(ENV_FILE):$(RESET)\n"; \
		for variable in $$missing; do printf "  - %s\n" "$$variable"; done; \
		exit 1; \
	fi; \
	printf "$(GREEN)✓ Environment is valid: $(ENV_FILE)$(RESET)\n"

validate: check-env ## Validate the complete merged Compose configuration
	@$(DC_ALL) config --quiet
	@printf "$(GREEN)✓ Compose configuration is valid$(RESET)\n"

config: check-env ## Print the complete merged Compose configuration
	@$(DC_ALL) config

ps: ## Show raw Docker container list for Mnemos
	@docker ps -a \
		--filter "label=com.docker.compose.project=$(PROJECT_NAME)" \
		--format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

urls: check-env ## Show configured local and public endpoints
	@set -a; source "$(ENV_FILE)"; set +a; \
	printf "Account API:  http://localhost:%s\n" "$${MNEMOS_ACCOUNT_SERVICE_HOST_PORT:-8081}"; \
	printf "Keycloak:     http://localhost:%s\n" "$${KEYCLOAK_HOST_PORT:-8080}"; \
	printf "PostgreSQL:   localhost:%s\n" "$${APP_POSTGRES_HOST_PORT}"; \
	printf "Kafka:        localhost:%s\n" "$${KAFKA_HOST_PORT}"; \
	[ -n "$${MNEMOS_FRONTEND_ORIGIN:-}" ] && printf "Frontend:     %s\n" "$$MNEMOS_FRONTEND_ORIGIN" || true; \
	[ -n "$${MNEMOS_API_ORIGIN:-}" ] && printf "API:          %s\n" "$$MNEMOS_API_ORIGIN" || true; \
	[ -n "$${MNEMOS_AUTH_ORIGIN:-}" ] && printf "Auth:         %s\n" "$$MNEMOS_AUTH_ORIGIN" || true

status: ## Show compact stack status in one box
	@set -a; [ -f "$(ENV_FILE)" ] && source "$(ENV_FILE)"; set +a; \
	project="$${COMPOSE_PROJECT_NAME:-$(PROJECT_NAME)}"; \
	width=$(BOX_WIDTH); \
	line() { printf "├"; printf '─%.0s' $$(seq 1 $$((width - 2))); printf "┤\n"; }; \
	top() { printf "╭"; printf '─%.0s' $$(seq 1 $$((width - 2))); printf "╮\n"; }; \
	bottom() { printf "╰"; printf '─%.0s' $$(seq 1 $$((width - 2))); printf "╯\n"; }; \
	row() { printf "│ %-74s │\n" "$$1"; }; \
	state_icon() { \
		case "$$1" in \
			running) printf "●" ;; \
			exited|dead) printf "●" ;; \
			created|restarting|paused) printf "◐" ;; \
			*) printf "○" ;; \
		esac; \
	}; \
	state_color() { \
		case "$$1" in \
			running) printf "$(GREEN)" ;; \
			exited|dead) printf "$(RED)" ;; \
			created|restarting|paused) printf "$(YELLOW)" ;; \
			*) printf "$(DIM)" ;; \
		esac; \
	}; \
	top; \
	row "$(BOLD)Mnemos stack$(RESET)  project=$$project"; \
	line; \
	containers="$$(docker ps -aq --filter "label=com.docker.compose.project=$$project")"; \
	if [ -z "$$containers" ]; then \
		row "$(YELLOW)○ No containers found$(RESET)"; \
	else \
		for id in $$containers; do \
			service="$$(docker inspect --format '{{ index .Config.Labels "com.docker.compose.service" }}' $$id 2>/dev/null)"; \
			state="$$(docker inspect --format '{{.State.Status}}' $$id 2>/dev/null)"; \
			health="$$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}—{{end}}' $$id 2>/dev/null)"; \
			ports="$$(docker port $$id 2>/dev/null | awk -F' -> ' '{print $$2}' | sed 's/0.0.0.0://; s/\[::\]://' | sort -u | paste -sd, -)"; \
			[ -z "$$ports" ] && ports="—"; \
			icon="$$(state_icon "$$state")"; \
			color="$$(state_color "$$state")"; \
			printf "│  %b%s%b  %-25s %-10s health=%-9s port=%-9s │\n" \
				"$$color" "$$icon" "$(RESET)" "$$service" "$$state" "$$health" "$$ports"; \
		done | sort -k4; \
	fi; \
	line; \
	printf "│  %-18s %-53s │\n" "Local API" "http://localhost:$${MNEMOS_ACCOUNT_SERVICE_HOST_PORT:-8081}"; \
	printf "│  %-18s %-53s │\n" "Local Keycloak" "http://localhost:$${KEYCLOAK_HOST_PORT:-8080}"; \
	[ -n "$${MNEMOS_FRONTEND_ORIGIN:-}" ] && printf "│  %-18s %-53s │\n" "Frontend" "$$MNEMOS_FRONTEND_ORIGIN" || true; \
	[ -n "$${MNEMOS_API_ORIGIN:-}" ] && printf "│  %-18s %-53s │\n" "Public API" "$$MNEMOS_API_ORIGIN" || true; \
	[ -n "$${MNEMOS_AUTH_ORIGIN:-}" ] && printf "│  %-18s %-53s │\n" "Public Auth" "$$MNEMOS_AUTH_ORIGIN" || true; \
	bottom

# =============================================================================
##@ Project
# =============================================================================

build: ## Build the complete Gradle project
	@$(GRADLEW) build

test: ## Run all tests
	@$(GRADLEW) test

clean: ## Remove Gradle build output
	@$(GRADLEW) clean

# =============================================================================
##@ Full stack
# =============================================================================

up: validate account-build ## Start the complete stack
	@$(DC_ALL) up -d
	@$(MAKE) --no-print-directory status

stop: check-env ## Stop all stack services
	@$(DC_ALL) stop

down: check-env ## Remove stack containers and networks
	@$(DC_ALL) down --remove-orphans

restart: validate account-build ## Recreate the complete stack
	@$(DC_ALL) up -d --force-recreate
	@$(MAKE) --no-print-directory status

logs: check-env ## Follow logs from the complete stack
	@$(DC_ALL) logs -f --tail=120

# =============================================================================
##@ Infrastructure
# =============================================================================

infra-up: validate ## Start PostgreSQL, Keycloak and Kafka
	@$(DC_INFRA) up -d
	@$(MAKE) --no-print-directory status

infra-stop: check-env ## Stop infrastructure services
	@$(DC_INFRA) stop

infra-down: check-env ## Remove infrastructure containers
	@$(DC_INFRA) down --remove-orphans

infra-restart: validate ## Recreate infrastructure services
	@$(DC_INFRA) up -d --force-recreate
	@$(MAKE) --no-print-directory status

infra-logs: check-env ## Follow infrastructure logs
	@$(DC_INFRA) logs -f --tail=120

# =============================================================================
##@ PostgreSQL
# =============================================================================

postgres-up: check-env ## Start application and Keycloak PostgreSQL
	@$(DC_POSTGRES) up -d app-postgres keycloak-postgres

postgres-stop: check-env ## Stop both PostgreSQL services
	@$(DC_POSTGRES) stop app-postgres keycloak-postgres

postgres-logs: check-env ## Follow PostgreSQL logs
	@$(DC_POSTGRES) logs -f --tail=100 app-postgres keycloak-postgres

# =============================================================================
##@ Kafka
# =============================================================================

kafka-up: check-env ## Start Kafka
	@$(DC_INFRA) up -d kafka

kafka-stop: check-env ## Stop Kafka
	@$(DC_INFRA) stop kafka

kafka-logs: check-env ## Follow Kafka logs
	@$(DC_INFRA) logs -f --tail=100 kafka

# =============================================================================
##@ Keycloak
# =============================================================================

keycloak-up: check-env ## Start Keycloak and its database
	@$(DC_AUTH) up -d keycloak-postgres keycloak

keycloak-stop: check-env ## Stop Keycloak
	@$(DC_AUTH) stop keycloak

keycloak-restart: check-env ## Recreate Keycloak
	@$(DC_AUTH) up -d --force-recreate keycloak

keycloak-logs: check-env ## Follow Keycloak logs
	@$(DC_AUTH) logs -f --tail=120 keycloak

keycloak-config: check-env ## Apply declarative realm configuration
	@$(DC_KEYCLOAK_CONFIG) run --rm $(KEYCLOAK_CONFIG_SERVICE)

keycloak-log-level: check-env ## Recreate Keycloak with Telegram authenticator log level, e.g. LEVEL=DEBUG
	@set -eu; \
	level="$${LEVEL:-}"; \
	case "$$level" in \
		TRACE|DEBUG|INFO|WARN|ERROR) ;; \
		"") \
			printf "$(RED)✗ LEVEL is required$(RESET)\n"; \
			printf "  Example: make keycloak-log-level LEVEL=DEBUG\n"; \
			exit 1; \
			;; \
		*) \
			printf "$(RED)✗ Unsupported log level: %s$(RESET)\n" "$$level"; \
			printf "  Allowed: TRACE, DEBUG, INFO, WARN, ERROR\n"; \
			exit 1; \
			;; \
	esac; \
	printf "$(CYAN)Recreating Keycloak with Telegram log level: %s$(RESET)\n" "$$level"; \
	TELEGRAM_AUTH_LOG_LEVEL="$$level" $(DC_AUTH) up -d --force-recreate keycloak; \
	printf "$(GREEN)✓ Telegram authenticator log level: %s$(RESET)\n" "$$level"

# =============================================================================
##@ Account service
# =============================================================================

account-build: check-env ## Build account-service JAR and Docker image
	@printf "$(CYAN)Building account-service...$(RESET)\n"
	@$(GRADLEW) $(ACCOUNT_MODULE):bootJar
	@$(DC_ACCOUNT) build $(ACCOUNT_SERVICE)

account-up: account-build ## Build and start account-service
	@$(DC_ACCOUNT) up -d $(ACCOUNT_SERVICE)
	@$(MAKE) --no-print-directory status

account-stop: check-env ## Stop account-service
	@$(DC_ACCOUNT) stop $(ACCOUNT_SERVICE)

account-down: check-env ## Remove account-service container
	@$(DC_ACCOUNT) rm -f -s $(ACCOUNT_SERVICE)

account-restart: account-build ## Rebuild and recreate account-service
	@$(DC_ACCOUNT) up -d --force-recreate $(ACCOUNT_SERVICE)
	@$(MAKE) --no-print-directory status

account-logs: check-env ## Follow account-service logs
	@$(DC_ACCOUNT) logs -f --tail=150 $(ACCOUNT_SERVICE)

account-config: check-env ## Print merged account-service Compose configuration
	@$(DC_ACCOUNT) config


# =============================================================================
##@ Telegram authenticator
# =============================================================================

telegram-auth-build: ## Build Telegram Keycloak authenticator extension
	@printf "$(CYAN)Building Telegram authenticator extension...$(RESET)\n"
	@$(GRADLEW) $(TELEGRAM_AUTH_MODULE):build
	@printf "$(GREEN)✓ Telegram authenticator extension built$(RESET)\n"