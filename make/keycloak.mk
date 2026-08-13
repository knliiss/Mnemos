.PHONY: keycloak-import

keycloak-import: require-compose
	$(call title,keycloak,IMPORT)
	@$(DC) up \
		--no-build \
		--no-deps \
		--force-recreate \
		keycloak-config
	$(call success,keycloak,configuration imported)
