.PHONY: app-postgres-shell keycloak-postgres-shell

app-postgres-shell: require-compose
	@set -a; source "$(ENV_FILE)"; set +a; \
	$(DC) exec app-postgres \
		psql -U "$$APP_POSTGRES_USER" -d "$$APP_POSTGRES_DB"

keycloak-postgres-shell: require-compose
	@set -a; source "$(ENV_FILE)"; set +a; \
	$(DC) exec keycloak-postgres \
		psql -U "$$KEYCLOAK_POSTGRES_USER" -d "$$KEYCLOAK_POSTGRES_DB"
