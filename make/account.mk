# =============================================================================
##@ Account service
# =============================================================================

.PHONY: account-jar account-image account-build account-rebuild-image account-up \
	account-stop account-down account-restart account-logs account-errors \
	account-config account-shell account-health

account-jar: ## Build account-service boot JAR
	$(call info,Building account-service boot JAR)
	@$(GRADLEW) $(ACCOUNT_MODULE):bootJar

account-image: check-env ## Build account-service Docker image
	$(call info,Building account-service Docker image)
	@$(DC_ACCOUNT) build $(ACCOUNT_SERVICE)

account-build: check-env account-jar account-image ## Build account-service JAR and Docker image
	$(call success,account-service build completed)

account-rebuild-image: check-env ## Clean boot JAR and rebuild Docker image without cache
	@$(GRADLEW) $(ACCOUNT_MODULE):clean $(ACCOUNT_MODULE):bootJar
	@$(DC_ACCOUNT) build --no-cache $(ACCOUNT_SERVICE)

account-up: validate telegram-auth-build account-build ## Start infrastructure and account-service
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(DC_ALL) up -d --wait --wait-timeout $(WAIT_TIMEOUT) $(ACCOUNT_SERVICE)
	@$(MAKE) --no-print-directory status

account-stop: check-env ## Stop account-service
	@$(DC_ACCOUNT) stop $(ACCOUNT_SERVICE)

account-down: check-env ## Stop and remove account-service container
	@$(DC_ACCOUNT) rm -f -s $(ACCOUNT_SERVICE)

account-restart: validate telegram-auth-build account-build ## Rebuild and recreate account-service with infrastructure
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(DC_ALL) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) $(ACCOUNT_SERVICE)
	@$(MAKE) --no-print-directory status

account-logs: check-env ## Follow account-service logs
	@$(DC_ACCOUNT) logs -f --tail=$(LOG_TAIL) $(ACCOUNT_SERVICE)

account-errors: check-env ## Show recent account-service WARN and ERROR lines
	@$(DC_ACCOUNT) logs --tail=$(LOG_TAIL) $(ACCOUNT_SERVICE) 2>&1 \
		| grep -Ei 'WARN|ERROR|Exception|Caused by' || true

account-config: check-env ## Print merged account-service Compose configuration
	@$(DC_ACCOUNT) config

account-shell: check-env ## Open a shell inside account-service
	@$(DC_ACCOUNT) exec $(ACCOUNT_SERVICE) sh

account-health: check-env ## Query account-service actuator health
	@set -a; source "$(ENV_FILE)"; set +a; \
	port="$${MNEMOS_ACCOUNT_SERVICE_HOST_PORT:-$${ACCOUNT_SERVICE_PORT:-8081}}"; \
	curl -fsS "http://localhost:$$port/actuator/health"; \
	printf "\n"
