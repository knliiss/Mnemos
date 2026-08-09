# =============================================================================
##@ Wallet service
# =============================================================================

.PHONY: wallet-jar wallet-image wallet-build wallet-rebuild-image wallet-up \
	wallet-stop wallet-down wallet-restart wallet-logs wallet-errors \
	wallet-config wallet-shell wallet-health

wallet-jar: ## Build wallet-service boot JAR
	$(call info,Building wallet-service boot JAR)
	@$(GRADLEW) $(WALLET_MODULE):bootJar

wallet-image: check-env ## Build wallet-service Docker image
	$(call info,Building wallet-service Docker image)
	@$(DC_WALLET) build $(WALLET_SERVICE)

wallet-build: check-env wallet-jar wallet-image ## Build wallet-service JAR and Docker image
	$(call success,wallet-service build completed)

wallet-rebuild-image: check-env ## Clean boot JAR and rebuild Docker image without cache
	@$(GRADLEW) $(WALLET_MODULE):clean $(WALLET_MODULE):bootJar
	@$(DC_WALLET) build --no-cache $(WALLET_SERVICE)

wallet-up: validate wallet-build ## Start infrastructure and wallet-service
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(DC_WALLET) up -d --wait --wait-timeout $(WAIT_TIMEOUT) $(WALLET_SERVICE)
	@$(MAKE) --no-print-directory status

wallet-stop: check-env ## Stop wallet-service
	@$(DC_WALLET) stop $(WALLET_SERVICE)

wallet-down: check-env ## Stop and remove wallet-service container
	@$(DC_WALLET) rm -f -s $(WALLET_SERVICE)

wallet-restart: validate wallet-build ## Rebuild and recreate wallet-service with infrastructure
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(DC_WALLET) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) $(WALLET_SERVICE)
	@$(MAKE) --no-print-directory status

wallet-logs: check-env ## Follow wallet-service logs
	@$(DC_WALLET) logs -f --tail=$(LOG_TAIL) $(WALLET_SERVICE)

wallet-errors: check-env ## Show recent wallet-service WARN and ERROR lines
	@$(DC_WALLET) logs --tail=$(LOG_TAIL) $(WALLET_SERVICE) 2>&1 \
		| grep -Ei 'WARN|ERROR|Exception|Caused by' || true

wallet-config: check-env ## Print merged wallet-service Compose configuration
	@$(DC_WALLET) config

wallet-shell: check-env ## Open a shell inside wallet-service
	@$(DC_WALLET) exec $(WALLET_SERVICE) sh

wallet-health: check-env ## Query wallet-service actuator health
	@set -a; source "$(ENV_FILE)"; set +a; \
	port="$${MNEMOS_WALLET_SERVICE_HOST_PORT:-8082}"; \
	curl -fsS "http://localhost:$$port/actuator/health"; \
	printf "\n"
