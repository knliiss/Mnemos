# =============================================================================
##@ Event service
# =============================================================================

.PHONY: event-jar event-image event-build event-rebuild-image event-up
	event-stop event-down event-restart event-logs event-errors \
	event-config event-shell event-health

event-jar: ## Build event-service boot JAR
	$(call info,Building event-service boot JAR)
	@$(GRADLEW) $(EVENT_MODULE):bootJar

event-image: check-env ## Build event-service Docker image
	$(call info,Building event-service Docker image)
	@$(DC_EVENT) build $(EVENT_SERVICE)

event-build: check-env event-jar event-image ## Build event-service JAR and Docker image
	$(call success,event-service build completed)

event-rebuild-image: check-env ## Clean boot JAR and rebuild Docker image without cache
	@$(GRADLEW) $(EVENT_MODULE):clean $(EVENT_MODULE):bootJar
	@$(DC_EVENT) build --no-cache $(EVENT_SERVICE)

event-up: validate event-build ## Start infrastructure and event-service
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(DC_EVENT) up -d --wait --wait-timeout $(WAIT_TIMEOUT) $(EVENT_SERVICE)
	@$(MAKE) --no-print-directory status

event-stop: check-env ## Stop event-service
	@$(DC_EVENT) stop $(EVENT_SERVICE)

event-down: check-env ## Stop and remove event-service container
	@$(DC_EVENT) rm -f -s $(EVENT_SERVICE)

event-restart: validate event-build ## Rebuild and recreate event-service with infrastructure
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(DC_EVENT) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) $(EVENT_SERVICE)
	@$(MAKE) --no-print-directory status

event-logs: check-env ## Follow event-service logs
	@$(DC_EVENT) logs -f --tail=$(LOG_TAIL) $(EVENT_SERVICE)

event-errors: check-env ## Show recent event-service WARN and ERROR lines
	@$(DC_EVENT) logs --tail=$(LOG_TAIL) $(EVENT_SERVICE) 2>&1 \
		| grep -Ei 'WARN|ERROR|Exception|Caused by' || true

event-config: check-env ## Print merged event-service Compose configuration
	@$(DC_EVENT) config

event-shell: check-env ## Open a shell inside event-service
	@$(DC_EVENT) exec $(EVENT_SERVICE) sh

event-health: check-env ## Query event-service actuator health
	@set -a; source "$(ENV_FILE)"; set +a; \
	port="$${MNEMOS_EVENT_SERVICE_HOST_PORT:-8083}"; \
	curl -fsS "http://localhost:$$port/actuator/health"; \
	printf "\n"
