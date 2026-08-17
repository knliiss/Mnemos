.PHONY: require-gradle require-docker require-env require-compose make-verify

require-gradle:
	@test -x "$(GRADLEW)" || { printf "\033[31m✗\033[0m %s is missing or not executable\n" "$(GRADLEW)" >&2; exit 2; }

require-docker:
	@command -v docker >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Docker is required\n" >&2; exit 2; }
	@docker compose version >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Docker Compose is required\n" >&2; exit 2; }

require-env:
	@test -f "$(ENV_FILE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(ENV_FILE)" >&2; exit 2; }

require-compose: require-docker require-env
	@test -f "$(BASE_COMPOSE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(BASE_COMPOSE)" >&2; exit 2; }

make-verify:
	$(call title,make,VERIFY)
	@set -e; \
	for service in $(APP_SERVICE_NAMES); do \
		if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$service"; then \
			printf "$(RED)✗$(RESET) Application %s is missing from Docker Compose\n" "$$service" >&2; \
			exit 1; \
		fi; \
	done; \
	for service in $(BUILDABLE_INFRA_SERVICE_NAMES) $(ONE_SHOT_SERVICE_NAMES) $(SHELLABLE_SERVICE_NAMES) $(PULLABLE_SERVICE_NAMES); do \
		if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$service"; then \
			printf "$(RED)✗$(RESET) Configured compose service %s is missing from Docker Compose\n" "$$service" >&2; \
			exit 1; \
		fi; \
	done; \
	targets="help build test check ktlintFormat clean deps up stop restart down logs pull status compose-config kafka-init-build kafka-topics kafka-groups kafka-create-make-verify kafka-delete-make-verify kafka-describe-make-verify app-postgres-shell keycloak-postgres-shell keycloak-import keycloak-restart completion-install completion-uninstall"; \
	for service in $(APP_NAMES); do \
		targets="$$targets $$service-build $$service-test $$service-check $$service-clean $$service-deps $$service-up $$service-stop $$service-restart $$service-status $$service-remove $$service-logs $$service-shell"; \
	done; \
	for service in $(COMPOSE_SERVICE_NAMES); do \
		targets="$$targets $$service-up $$service-stop $$service-restart $$service-status $$service-remove $$service-logs"; \
	done; \
	for service in $(SHELLABLE_SERVICE_NAMES); do \
		targets="$$targets $$service-shell"; \
	done; \
	for service in $(PULLABLE_SERVICE_NAMES); do \
		targets="$$targets $$service-pull"; \
	done; \
	for target in $$targets; do \
		$(MAKE) --no-print-directory -n "$$target" >/dev/null || { \
			printf "$(RED)✗$(RESET) Make target cannot be resolved: %s\n" "$$target" >&2; \
			exit 1; \
		}; \
	done
	$(call success,make,target wiring verified)
