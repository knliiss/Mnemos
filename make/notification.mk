# =============================================================================
##@ Notification service
# =============================================================================

.PHONY: notification-jar notification-image notification-build notification-rebuild-image notification-up
	notification-stop notification-down notification-restart notification-logs notification-errors \
	notification-config notification-shell notification-health

notification-jar: ## Build notification-service boot JAR
	$(call info,Building notification-service boot JAR)
	@$(GRADLEW) $(NOTIFICATION_MODULE):bootJar

notification-image: check-env ## Build notification-service Docker image
	$(call info,Building notification-service Docker image)
	@$(DC_NOTIFICATION) build $(NOTIFICATION_SERVICE)

notification-build: check-env notification-jar notification-image ## Build notification-service JAR and Docker image
	$(call success,notification-service build completed)

notification-rebuild-image: check-env ## Clean boot JAR and rebuild Docker image without cache
	@$(GRADLEW) $(NOTIFICATION_MODULE):clean $(NOTIFICATION_MODULE):bootJar
	@$(DC_NOTIFICATION) build --no-cache $(NOTIFICATION_SERVICE)

notification-up: validate notification-build ## Start infrastructure and notification-service
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(DC_NOTIFICATION) up -d --wait --wait-timeout $(WAIT_TIMEOUT) $(NOTIFICATION_SERVICE)
	@$(MAKE) --no-print-directory status

notification-stop: check-env ## Stop notification-service
	@$(DC_NOTIFICATION) stop $(NOTIFICATION_SERVICE)

notification-down: check-env ## Stop and remove notification-service container
	@$(DC_NOTIFICATION) rm -f -s $(NOTIFICATION_SERVICE)

notification-restart: validate notification-build ## Rebuild and recreate notification-service with infrastructure
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(DC_NOTIFICATION) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) $(NOTIFICATION_SERVICE)
	@$(MAKE) --no-print-directory status

notification-logs: check-env ## Follow notification-service logs
	@$(DC_NOTIFICATION) logs -f --tail=$(LOG_TAIL) $(NOTIFICATION_SERVICE)

notification-errors: check-env ## Show recent notification-service WARN and ERROR lines
	@$(DC_NOTIFICATION) logs --tail=$(LOG_TAIL) $(NOTIFICATION_SERVICE) 2>&1 \
		| grep -Ei 'WARN|ERROR|Exception|Caused by' || true

notification-config: check-env ## Print merged notification-service Compose configuration
	@$(DC_NOTIFICATION) config

notification-shell: check-env ## Open a shell inside notification-service
	@$(DC_NOTIFICATION) exec $(NOTIFICATION_SERVICE) sh

notification-health: check-env ## Query notification-service actuator health
	@set -a; source "$(ENV_FILE)"; set +a; \
	port="$${MNEMOS_NOTIFICATION_SERVICE_HOST_PORT:-8083}"; \
	curl -fsS "http://localhost:$$port/actuator/health"; \
	printf "\n"
