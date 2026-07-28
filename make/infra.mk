# =============================================================================
##@ Infrastructure
# =============================================================================

.PHONY: infra-up infra-stop infra-down infra-restart infra-logs

infra-up: validate telegram-auth-build ## Build provider and start all infrastructure services
	$(call info,Starting infrastructure)
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory keycloak-provider-status
	@$(MAKE) --no-print-directory status

infra-stop: check-env ## Stop infrastructure without removing containers
	@$(DC_INFRA) stop

infra-down: check-env ## Remove infrastructure containers
	@$(DC_INFRA) down --remove-orphans

infra-restart: validate telegram-auth-build ## Rebuild provider and recreate infrastructure
	$(call info,Recreating infrastructure)
	@$(DC_INFRA) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory keycloak-provider-status
	@$(MAKE) --no-print-directory status

infra-logs: check-env ## Follow infrastructure logs
	@$(DC_INFRA) logs -f --tail=$(LOG_TAIL)