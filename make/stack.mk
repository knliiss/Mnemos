# =============================================================================
##@ Full stack
# =============================================================================

.PHONY: up stop down restart rebuild pull logs reset

up: validate telegram-auth-build account-build wallet-build ## Build and start the complete stack
	$(call info,Starting complete stack)
	@$(DC_ALL) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory status

stop: check-env ## Stop all stack services without removing containers
	@$(DC_ALL) stop

down: check-env ## Remove stack containers and orphaned containers
	@$(DC_ALL) down --remove-orphans

restart: validate telegram-auth-build account-build wallet-build ## Rebuild artifacts and recreate the complete stack
	$(call info,Recreating complete stack)
	@$(DC_ALL) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory status

rebuild: validate telegram-auth-clean telegram-auth-build account-rebuild-image wallet-rebuild-image ## Clean-build artifacts and recreate the stack
	$(call info,Recreating complete stack from clean application artifacts)
	@$(DC_ALL) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory status

pull: check-env ## Pull infrastructure images
	@$(DC_INFRA) pull
	@$(DC_KEYCLOAK_CONFIG) pull

logs: check-env ## Follow logs from the complete stack
	@$(DC_ALL) logs -f --tail=$(LOG_TAIL)

reset: check-env ## Remove containers and volumes; requires CONFIRM=1
	@if [ "$(CONFIRM)" != "1" ]; then \
		printf "$(RED)Refusing destructive reset. Run: make reset CONFIRM=1$(RESET)\n"; \
		exit 1; \
	fi
	@$(DC_ALL) down -v --remove-orphans
	$(call success,Containers, networks and volumes were removed)
