# =============================================================================
##@ PostgreSQL
# =============================================================================

.PHONY: postgres-up postgres-stop postgres-restart postgres-logs postgres-status postgres-psql-app postgres-psql-keycloak

postgres-up: check-env ## Start application and Keycloak PostgreSQL
	@$(DC_POSTGRES) up -d --wait --wait-timeout $(WAIT_TIMEOUT) \
		$(APP_POSTGRES_SERVICE) $(KEYCLOAK_POSTGRES_SERVICE)
	@$(MAKE) --no-print-directory status

postgres-stop: check-env ## Stop both PostgreSQL services
	@$(DC_POSTGRES) stop $(APP_POSTGRES_SERVICE) $(KEYCLOAK_POSTGRES_SERVICE)

postgres-restart: check-env ## Recreate both PostgreSQL services
	@$(DC_POSTGRES) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) \
		$(APP_POSTGRES_SERVICE) $(KEYCLOAK_POSTGRES_SERVICE)
	@$(MAKE) --no-print-directory status

postgres-logs: check-env ## Follow both PostgreSQL logs
	@$(DC_POSTGRES) logs -f --tail=$(LOG_TAIL) \
		$(APP_POSTGRES_SERVICE) $(KEYCLOAK_POSTGRES_SERVICE)

postgres-status: check-env ## Run pg_isready in both PostgreSQL containers
	@$(DC_POSTGRES) exec -T $(APP_POSTGRES_SERVICE) sh -lc 'pg_isready -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'
	@$(DC_POSTGRES) exec -T $(KEYCLOAK_POSTGRES_SERVICE) sh -lc 'pg_isready -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'

postgres-psql-app: check-env ## Open psql for the application database
	@$(DC_POSTGRES) exec $(APP_POSTGRES_SERVICE) sh -lc 'psql -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'

postgres-psql-keycloak: check-env ## Open psql for the Keycloak database
	@$(DC_POSTGRES) exec $(KEYCLOAK_POSTGRES_SERVICE) sh -lc 'psql -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'
