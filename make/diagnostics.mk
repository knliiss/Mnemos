# =============================================================================
##@ Diagnostics
# =============================================================================

.PHONY: help status ps urls logs-errors

help: ## Show available commands
	@printf "\n$(BOLD)$(CYAN)Mnemos development toolkit$(RESET)\n"
	@printf "$(DIM)Infrastructure, builds, applications and diagnostics$(RESET)\n\n"
	@awk '\
		BEGIN { FS = ":.*## " } \
		/^##@ / { \
			if (printed) printf "\n"; \
			printf "$(BOLD)$(MAGENTA)%s$(RESET)\n", substr($$0, 5); \
			printed = 1; \
			next \
		} \
		/^[a-zA-Z0-9_.-]+:.*## / { \
			printf "  $(GREEN)%-27s$(RESET) %s\n", $$1, $$2 \
		} \
	' $(MAKEFILE_LIST)
	@printf "\n$(DIM)Examples$(RESET)\n"
	@printf "  $(CYAN)make infra-up$(RESET)              build the Keycloak provider and start infrastructure\n"
	@printf "  $(CYAN)make account-up$(RESET)            build and start account-service with all dependencies\n"
	@printf "  $(CYAN)make wallet-up$(RESET)             build and start wallet-service with all dependencies\n"
	@printf "  $(CYAN)make event-up$(RESET)              build and start event-service with all dependencies\n"
	@printf "  $(CYAN)make keycloak-restart$(RESET)      rebuild the provider before recreating Keycloak\n"
	@printf "  $(CYAN)make kafka-topics$(RESET)          list Kafka topics\n"
	@printf "  $(CYAN)make status$(RESET)                show health, restarts, ports and provider state\n"
	@printf "  $(CYAN)make doctor$(RESET)                validate the complete local environment\n\n"

ps: check-env ## Show raw Compose service state
	@$(DC_ALL) ps --all

status: check-env ## Show detailed stack, endpoint and provider status
	@set -a; source "$(ENV_FILE)"; set +a; \
	project="$${COMPOSE_PROJECT_NAME:-$(PROJECT_NAME)}"; \
	printf "\n$(BOLD)$(CYAN)Mnemos status$(RESET)  project=%s\n\n" "$$project"; \
	printf "$(BOLD)%-26s %-20s %-11s %-11s %-8s %s$(RESET)\n" \
		"SERVICE" "CONTAINER" "STATE" "HEALTH" "RESTARTS" "PORTS"; \
	printf '%s\n' "--------------------------------------------------------------------------------------------------------------"; \
	ids="$$(docker ps -aq --filter "label=com.docker.compose.project=$$project")"; \
	if [ -z "$$ids" ]; then \
		printf "$(YELLOW)No containers found for project %s$(RESET)\n" "$$project"; \
	else \
		tmp="$$(mktemp)"; \
		trap 'rm -f "$$tmp"' EXIT; \
		for id in $$ids; do \
			service="$$(docker inspect --format '{{index .Config.Labels "com.docker.compose.service"}}' "$$id" 2>/dev/null)"; \
			name="$$(docker inspect --format '{{.Name}}' "$$id" 2>/dev/null | sed 's#^/##')"; \
			state="$$(docker inspect --format '{{.State.Status}}' "$$id" 2>/dev/null)"; \
			health="$$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}' "$$id" 2>/dev/null)"; \
			restarts="$$(docker inspect --format '{{.RestartCount}}' "$$id" 2>/dev/null)"; \
			ports="$$(docker port "$$id" 2>/dev/null | awk -F' -> ' '{print $$2}' | sed 's/0.0.0.0://; s/\[::\]://' | sort -u | paste -sd, -)"; \
			[ -n "$$ports" ] || ports="-"; \
			printf "%s|%s|%s|%s|%s|%s\n" "$$service" "$$name" "$$state" "$$health" "$$restarts" "$$ports" >> "$$tmp"; \
		done; \
		sort "$$tmp" | while IFS='|' read -r service name state health restarts ports; do \
			state_color="$(YELLOW)"; \
			[ "$$state" = "running" ] && state_color="$(GREEN)"; \
			[ "$$state" = "exited" ] && state_color="$(RED)"; \
			health_color="$(DIM)"; \
			[ "$$health" = "healthy" ] && health_color="$(GREEN)"; \
			[ "$$health" = "unhealthy" ] && health_color="$(RED)"; \
			printf "%-26s %-20s %b%-11s$(RESET) %b%-11s$(RESET) %-8s %s\n" \
				"$$service" "$$name" "$$state_color" "$$state" "$$health_color" "$$health" "$$restarts" "$$ports"; \
		done; \
	fi; \
	printf "\n$(BOLD)Provider$(RESET)\n"; \
	if [ -f "$(TELEGRAM_AUTH_JAR)" ]; then \
		size="$$(wc -c < "$(TELEGRAM_AUTH_JAR)" | tr -d ' ')"; \
		printf "  local JAR       $(GREEN)present$(RESET)  %s bytes\n" "$$size"; \
	else \
		printf "  local JAR       $(RED)missing$(RESET)  %s\n" "$(TELEGRAM_AUTH_JAR)"; \
	fi; \
	if $(DC_AUTH) ps --status running -q $(KEYCLOAK_SERVICE) 2>/dev/null | grep -q .; then \
		if $(DC_AUTH) exec -T $(KEYCLOAK_SERVICE) sh -lc 'test -f "$(KEYCLOAK_PROVIDER_PATH)"' >/dev/null 2>&1; then \
			remote_size="$$( $(DC_AUTH) exec -T $(KEYCLOAK_SERVICE) sh -lc 'wc -c < "$(KEYCLOAK_PROVIDER_PATH)"' 2>/dev/null | tr -d '[:space:]' )"; \
			printf "  container JAR   $(GREEN)mounted$(RESET)  %s bytes\n" "$$remote_size"; \
		else \
			printf "  container JAR   $(RED)missing$(RESET)\n"; \
		fi; \
	else \
		printf "  container JAR   $(DIM)Keycloak is not running$(RESET)\n"; \
	fi; \
	printf "\n$(BOLD)Endpoints$(RESET)\n"; \
	account_port="$${MNEMOS_ACCOUNT_SERVICE_HOST_PORT:-$${ACCOUNT_SERVICE_PORT:-8081}}"; \
	printf "  Account API     http://localhost:%s\n" "$$account_port"; \
	wallet_port="$${MNEMOS_WALLET_SERVICE_HOST_PORT:-8082}"; \
	printf "  Wallet API      http://localhost:%s\n" "$$wallet_port"; \
	printf "  Keycloak        http://localhost:%s\n" "$${KEYCLOAK_HOST_PORT:-8080}"; \
	printf "  App PostgreSQL  localhost:%s\n" "$${APP_POSTGRES_HOST_PORT:-5433}"; \
	printf "  KC PostgreSQL   localhost:%s\n" "$${KEYCLOAK_POSTGRES_HOST_PORT:-5434}"; \
	printf "  Kafka           localhost:%s\n" "$${KAFKA_HOST_PORT:-9092}"; \
	[ -n "$${MNEMOS_FRONTEND_ORIGIN:-}" ] && printf "  Frontend        %s\n" "$$MNEMOS_FRONTEND_ORIGIN" || true; \
	[ -n "$${MNEMOS_API_ORIGIN:-}" ] && printf "  Public API      %s\n" "$$MNEMOS_API_ORIGIN" || true; \
	[ -n "$${MNEMOS_AUTH_ORIGIN:-}" ] && printf "  Public Auth     %s\n" "$$MNEMOS_AUTH_ORIGIN" || true; \
	printf "\n"

urls: check-env ## Show configured local and public endpoints
	@set -a; source "$(ENV_FILE)"; set +a; \
	account_port="$${MNEMOS_ACCOUNT_SERVICE_HOST_PORT:-$${ACCOUNT_SERVICE_PORT:-8081}}"; \
	printf "Account API:      http://localhost:%s\n" "$$account_port"; \
	wallet_port="$${MNEMOS_WALLET_SERVICE_HOST_PORT:-8082}"; \
	printf "Wallet API:       http://localhost:%s\n" "$$wallet_port"; \
	printf "Keycloak:         http://localhost:%s\n" "$${KEYCLOAK_HOST_PORT:-8080}"; \
	printf "App PostgreSQL:   localhost:%s\n" "$${APP_POSTGRES_HOST_PORT:-5433}"; \
	printf "KC PostgreSQL:    localhost:%s\n" "$${KEYCLOAK_POSTGRES_HOST_PORT:-5434}"; \
	printf "Kafka:            localhost:%s\n" "$${KAFKA_HOST_PORT:-9092}"; \
	[ -n "$${MNEMOS_FRONTEND_ORIGIN:-}" ] && printf "Frontend:         %s\n" "$$MNEMOS_FRONTEND_ORIGIN" || true; \
	[ -n "$${MNEMOS_API_ORIGIN:-}" ] && printf "Public API:       %s\n" "$$MNEMOS_API_ORIGIN" || true; \
	[ -n "$${MNEMOS_AUTH_ORIGIN:-}" ] && printf "Public Auth:      %s\n" "$$MNEMOS_AUTH_ORIGIN" || true

logs-errors: check-env ## Show recent WARN and ERROR lines from the complete stack
	@$(DC_ALL) logs --tail=$(LOG_TAIL) 2>&1 | grep -Ei 'WARN|ERROR|Exception|Caused by' || true
