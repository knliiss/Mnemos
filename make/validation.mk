# =============================================================================
##@ Validation
# =============================================================================

.PHONY: check-tools check-env validate doctor config config-infra config-account config-wallet config-keycloak

check-tools: ## Check required local tools
	@set -eu; \
	missing=""; \
	for tool in docker java; do \
		command -v "$$tool" >/dev/null 2>&1 || missing="$$missing $$tool"; \
	done; \
	if [ ! -x "$(GRADLEW)" ]; then missing="$$missing $(GRADLEW)"; fi; \
	if ! docker compose version >/dev/null 2>&1; then missing="$$missing docker-compose-plugin"; fi; \
	if [ -n "$$missing" ]; then \
		printf "$(RED)✗ Missing required tools:$(RESET)%s\n" "$$missing"; \
		exit 1; \
	fi; \
	printf "$(GREEN)✓ Required tools are available$(RESET)\n"

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
		WALLET_DATASOURCE_URL \
		KEYCLOAK_POSTGRES_DB \
		KEYCLOAK_POSTGRES_USER \
		KEYCLOAK_POSTGRES_PASSWORD \
		KEYCLOAK_POSTGRES_HOST_PORT \
		KEYCLOAK_VERSION \
		KEYCLOAK_HOST_PORT \
		KEYCLOAK_ADMIN_USERNAME \
		KEYCLOAK_ADMIN_PASSWORD \
		KEYCLOAK_CONFIG_CLI_VERSION \
		KAFKA_VERSION \
		KAFKA_HOST_PORT \
		REDIS_VERSION \
		REDIS_HOST_PORT \
		REDIS_PASSWORD \
		REDIS_MAXMEMORY \
		MNEMOS_TELEGRAM_BOT_TOKEN \
		MNEMOS_FRONTEND_ORIGIN \
		MNEMOS_ACCOUNT_SERVICE_CLIENT_ID \
		KEYCLOAK_ACCOUNT_SERVICE_CLIENT_SECRET; do \
		eval "value=\$${variable:-}"; \
		if [ -z "$$value" ]; then missing="$$missing $$variable"; fi; \
	done; \
	if [ -n "$$missing" ]; then \
		printf "$(RED)✗ Missing required variables in $(ENV_FILE):$(RESET)\n"; \
		for variable in $$missing; do printf "  - %s\n" "$$variable"; done; \
		exit 1; \
	fi; \
	printf "$(GREEN)✓ Environment is valid: $(ENV_FILE)$(RESET)\n"

validate: check-tools check-env ## Validate all merged Compose configurations
	@$(DC_ALL) config --quiet
	@$(DC_KEYCLOAK_CONFIG) config --quiet
	$(call success,Compose configurations are valid)

doctor: check-tools check-env ## Run complete local development diagnostics
	@printf "\n$(BOLD)Mnemos doctor$(RESET)\n\n"
	@$(DC_ALL) config --quiet
	@$(DC_KEYCLOAK_CONFIG) config --quiet
	@printf "$(GREEN)✓ Compose configuration$(RESET)\n"
	@if [ -f "$(TELEGRAM_AUTH_JAR)" ]; then \
		printf "$(GREEN)✓ Telegram authenticator JAR$(RESET)  %s\n" "$(TELEGRAM_AUTH_JAR)"; \
	else \
		printf "$(YELLOW)! Telegram authenticator JAR is not built$(RESET)\n"; \
	fi
	@if docker info >/dev/null 2>&1; then \
		printf "$(GREEN)✓ Docker daemon is available$(RESET)\n"; \
	else \
		printf "$(RED)✗ Docker daemon is unavailable$(RESET)\n"; \
		exit 1; \
	fi
	@printf "\n"
	@$(MAKE) --no-print-directory status

config: check-env ## Print complete merged Compose configuration
	@$(DC_ALL) config

config-infra: check-env ## Print merged infrastructure Compose configuration
	@$(DC_INFRA) config

config-account: check-env ## Print merged account-service Compose configuration
	@$(DC_ACCOUNT) config

config-wallet: check-env ## Print merged wallet-service Compose configuration
	@$(DC_WALLET) config

config-keycloak: check-env ## Print merged Keycloak configuration runner Compose configuration
	@$(DC_KEYCLOAK_CONFIG) config
