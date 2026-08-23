.PHONY: keycloak-import keycloak-restart

keycloak-import: require-compose
	$(call title,keycloak,IMPORT)
	@$(DC) up \
		--no-build \
		--no-deps \
		--force-recreate \
		keycloak-config
	$(call verify_containers,keycloak-config)
	$(call success,keycloak,configuration imported)

keycloak-restart: require-gradle require-compose
	$(call title,keycloak,RESTART)
	$(call run_step,Authenticator,$(GRADLEW) :keycloak-extensions:telegram-authenticator:build --console=plain)
	$(call run_step,Recreate,$(DC) up -d --no-deps --force-recreate --no-build keycloak)
	$(call verify_containers,keycloak)
	$(call success,keycloak,running with current authenticator)
