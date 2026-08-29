.PHONY: require-gradle require-docker require-env require-compose require-node make-verify

require-gradle:
	@test -x "$(GRADLEW)" || { printf "\033[31m✗\033[0m %s is missing or not executable\n" "$(GRADLEW)" >&2; exit 2; }

require-docker:
	@command -v docker >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Docker is required\n" >&2; exit 2; }
	@docker compose version >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Docker Compose is required\n" >&2; exit 2; }

require-env:
	@test -f "$(ENV_FILE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(ENV_FILE)" >&2; exit 2; }

require-compose: require-docker require-env
	@test -f "$(BASE_COMPOSE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(BASE_COMPOSE)" >&2; exit 2; }
	@test -f "$(COMPOSE_ENV_WRAPPER)" || { printf "\033[31m✗\033[0m %s not found\n" "$(COMPOSE_ENV_WRAPPER)" >&2; exit 2; }

require-node:
	@command -v node >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Node.js is required\n" >&2; exit 2; }
	@command -v npm >/dev/null 2>&1 || { printf "\033[31m✗\033[0m npm is required\n" >&2; exit 2; }
	@test -f "$(MINI_APP_NODE_VERSION_FILE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(MINI_APP_NODE_VERSION_FILE)" >&2; exit 2; }
	@expected="$$(tr -d '[:space:]' < "$(MINI_APP_NODE_VERSION_FILE)")"; \
	actual="$$(node -p 'process.versions.node')"; \
	if [ "$$actual" != "$$expected" ]; then \
		printf "\033[31m✗\033[0m Mini App requires Node.js %s, current version is %s\n" "$$expected" "$$actual" >&2; \
		exit 2; \
	fi

make-verify:
	$(call title,make,VERIFY)
	@test -f "$(COMPOSE_ENV_WRAPPER)" || { printf "$(RED)✗$(RESET) Compose environment wrapper is missing: %s\n" "$(COMPOSE_ENV_WRAPPER)" >&2; exit 1; }
	@test -f "$(MANUAL_TEST_ENV_PREPARE)" || { printf "$(RED)✗$(RESET) Manual test environment helper is missing: %s\n" "$(MANUAL_TEST_ENV_PREPARE)" >&2; exit 1; }
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
	targets="help build test check ktlintFormat clean deps test-env-prepare test-ready mini-app-install mini-app-verify mini-app-dev up stop restart down logs pull status compose-config kafka-init-build kafka-message-auth-sync kafka-message-auth-verify kafka-message-auth-runtime-verify kafka-topics kafka-groups kafka-create-make-verify kafka-delete-make-verify kafka-describe-make-verify app-postgres-shell keycloak-postgres-shell keycloak-import keycloak-restart completion-install completion-uninstall"; \
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
	@sh -n scripts/wait-production-service.sh
	@sh -n scripts/wait-production-container.sh
	@sh -n scripts/verify-production-deployment-executor-security.sh
	@sh -n scripts/verify-production-deployment-workflow-boundary.sh
	@python3 -m py_compile scripts/resolve_backend_ci_changes.py
	@python3 -m py_compile scripts/verify-backend-ci-changes.py
	@python3 -m py_compile scripts/production_deployment_executor.py
	@python3 -m py_compile scripts/production_deployment_store.py
	@python3 -m py_compile scripts/production_release_plan.py
	@python3 -m py_compile scripts/verify-production-release-plan.py
	@python3 -m py_compile scripts/verify-production-runtime-baseline.py
	@python3 -m py_compile scripts/production_runtime_state.py
	@python3 scripts/verify-backend-ci-changes.py
	@python3 scripts/verify-production-swarm-stack-normalizer.py
	@python3 scripts/verify-production-service-catalog.py
	@python3 scripts/verify-production-release-manifest.py
	@python3 scripts/verify-production-service-changes.py
	@python3 scripts/verify-production-deployment-plan.py
	@python3 scripts/verify-production-deployment-state.py
	@python3 scripts/verify-production-deployment-store.py
	@python3 scripts/verify-production-hotfix-plan.py
	@python3 scripts/verify-production-release-plan.py
	@python3 scripts/verify-production-deployment-executor.py
	@python3 scripts/verify-production-runtime-state.py
	@python3 scripts/verify-production-stack-manifest-renderer.py
	@sh scripts/verify-production-hotfix-plan-security.sh
	@sh scripts/verify-production-service-image-security.sh
	@sh scripts/verify-production-deployment-executor-security.sh
	@sh scripts/verify-production-deployment-workflow-boundary.sh
	@set -e; \
	for service in $$(python3 scripts/production_service_catalog.py list); do \
		python3 scripts/publish-production-service-image.py \
			--service "$$service" \
			--registry "example.invalid/mnemos" \
			--tag "0000000000000000000000000000000000000000" \
			--dry-run \
			--skip-assemble >/dev/null; \
	done
	$(call success,make,target wiring verified)
