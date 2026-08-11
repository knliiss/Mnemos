.PHONY: keycloak-import
keycloak-import: validate
	$(call step,Running Keycloak configuration import)
	@$(DC) up --no-build --no-deps --force-recreate keycloak-config
