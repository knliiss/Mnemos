# =============================================================================
##@ Keycloak
# =============================================================================

.PHONY: telegram-auth-clean telegram-auth-build telegram-auth-verify telegram-auth-info \
	keycloak-up keycloak-stop keycloak-restart keycloak-logs keycloak-errors \
	keycloak-config keycloak-provider-status

telegram-auth-clean: ## Clean Telegram authenticator build output
	@$(GRADLEW) $(TELEGRAM_AUTH_MODULE):clean
	@if [ -d "$(TELEGRAM_AUTH_JAR)" ]; then rm -rf "$(TELEGRAM_AUTH_JAR)"; fi

telegram-auth-build: ## Build and verify the Telegram Keycloak authenticator
	@if [ -d "$(TELEGRAM_AUTH_JAR)" ]; then \
		printf "$(YELLOW)! Removing directory created at the expected JAR path$(RESET)\n"; \
		rm -rf "$(TELEGRAM_AUTH_JAR)"; \
	fi
	$(call info,Building Telegram authenticator)
	@$(GRADLEW) $(TELEGRAM_AUTH_MODULE):build
	@$(MAKE) --no-print-directory telegram-auth-verify

telegram-auth-verify: ## Verify JAR and AuthenticatorFactory service descriptor
	@set -eu; \
	jar_path="$(TELEGRAM_AUTH_JAR)"; \
	if [ ! -f "$$jar_path" ]; then \
		printf "$(RED)✗ Authenticator JAR not found: %s$(RESET)\n" "$$jar_path"; \
		exit 1; \
	fi; \
	if ! jar tf "$$jar_path" | grep -qx "$(TELEGRAM_AUTH_SERVICE_FILE)"; then \
		printf "$(RED)✗ Missing %s in %s$(RESET)\n" "$(TELEGRAM_AUTH_SERVICE_FILE)" "$$jar_path"; \
		exit 1; \
	fi; \
	factory="$$(unzip -p "$$jar_path" "$(TELEGRAM_AUTH_SERVICE_FILE)" \
		| sed 's/#.*//' \
		| sed '/^[[:space:]]*$$/d' \
		| head -n 1 \
		| tr -d '\r')"; \
	if [ -z "$$factory" ]; then \
		printf "$(RED)✗ AuthenticatorFactory descriptor is empty$(RESET)\n"; \
		exit 1; \
	fi; \
	printf "$(GREEN)✓ Telegram authenticator JAR is valid$(RESET)\n"; \
	printf "  JAR:     %s\n" "$$jar_path"; \
	printf "  Factory: %s\n" "$$factory"

telegram-auth-info: ## Show local authenticator artifact information
	@$(MAKE) --no-print-directory telegram-auth-verify
	@size="$$(wc -c < "$(TELEGRAM_AUTH_JAR)" | tr -d ' ')"; \
	if stat -f '%Sm' -t '%Y-%m-%d %H:%M:%S' "$(TELEGRAM_AUTH_JAR)" >/dev/null 2>&1; then \
		modified="$$(stat -f '%Sm' -t '%Y-%m-%d %H:%M:%S' "$(TELEGRAM_AUTH_JAR)")"; \
	else \
		modified="$$(stat -c '%y' "$(TELEGRAM_AUTH_JAR)" | cut -d. -f1)"; \
	fi; \
	printf "  Size:     %s bytes\n" "$$size"; \
	printf "  Modified: %s\n" "$$modified"

keycloak-up: check-env telegram-auth-build ## Build provider, start Keycloak and apply realm config
	@$(DC_AUTH) up -d --wait --wait-timeout $(WAIT_TIMEOUT) \
		$(KEYCLOAK_POSTGRES_SERVICE) $(KEYCLOAK_SERVICE)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory keycloak-provider-status
	@$(MAKE) --no-print-directory status

keycloak-stop: check-env ## Stop Keycloak
	@$(DC_AUTH) stop $(KEYCLOAK_SERVICE)

keycloak-restart: check-env telegram-auth-build ## Rebuild provider, recreate Keycloak and apply realm config
	@$(DC_AUTH) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) \
		$(KEYCLOAK_POSTGRES_SERVICE) $(KEYCLOAK_SERVICE)
	@$(MAKE) --no-print-directory keycloak-config
	@$(MAKE) --no-print-directory keycloak-provider-status
	@$(MAKE) --no-print-directory status

keycloak-logs: check-env ## Follow Keycloak logs
	@$(DC_AUTH) logs -f --tail=$(LOG_TAIL) $(KEYCLOAK_SERVICE)

keycloak-errors: check-env ## Show recent Keycloak authentication and provider errors
	@$(DC_AUTH) logs --tail=$(LOG_TAIL) $(KEYCLOAK_SERVICE) 2>&1 \
		| grep -Ei 'ERROR|WARN|LOGIN_ERROR|AuthenticatorFactory|AuthenticationFlow|Caused by' || true

keycloak-config: check-env ## Wait for Keycloak and apply declarative realm configuration
	@$(DC_AUTH) up -d --wait --wait-timeout $(WAIT_TIMEOUT) \
		$(KEYCLOAK_POSTGRES_SERVICE) $(KEYCLOAK_SERVICE)
	$(call info,Applying declarative Keycloak configuration)
	@$(DC_KEYCLOAK_CONFIG) run --rm $(KEYCLOAK_CONFIG_SERVICE)
	$(call success,Keycloak configuration applied)

keycloak-provider-status: check-env ## Verify provider JAR locally and inside Keycloak
	@$(MAKE) --no-print-directory telegram-auth-verify
	@if ! $(DC_AUTH) ps --status running -q $(KEYCLOAK_SERVICE) | grep -q .; then \
		printf "$(RED)✗ Keycloak is not running$(RESET)\n"; \
		exit 1; \
	fi
	@if $(DC_AUTH) exec -T $(KEYCLOAK_SERVICE) sh -lc 'test -f "$(KEYCLOAK_PROVIDER_PATH)"'; then \
		local_size="$$(wc -c < "$(TELEGRAM_AUTH_JAR)" | tr -d ' ')"; \
		remote_size="$$( $(DC_AUTH) exec -T $(KEYCLOAK_SERVICE) sh -lc 'wc -c < "$(KEYCLOAK_PROVIDER_PATH)"' | tr -d '[:space:]' )"; \
		if [ "$$local_size" != "$$remote_size" ]; then \
			printf "$(RED)✗ Provider size mismatch: local=%s container=%s$(RESET)\n" "$$local_size" "$$remote_size"; \
			exit 1; \
		fi; \
		printf "$(GREEN)✓ Provider is mounted in Keycloak$(RESET)  %s bytes\n" "$$remote_size"; \
	else \
		printf "$(RED)✗ Provider is missing inside Keycloak: %s$(RESET)\n" "$(KEYCLOAK_PROVIDER_PATH)"; \
		exit 1; \
	fi
