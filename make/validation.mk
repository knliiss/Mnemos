.PHONY: require-gradle require-docker require-env require-compose

require-gradle:
	@test -x "$(GRADLEW)" || { printf "\033[31m✗\033[0m %s is missing or not executable\n" "$(GRADLEW)" >&2; exit 2; }

require-docker:
	@command -v docker >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Docker is required\n" >&2; exit 2; }
	@docker compose version >/dev/null 2>&1 || { printf "\033[31m✗\033[0m Docker Compose is required\n" >&2; exit 2; }

require-env:
	@test -f "$(ENV_FILE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(ENV_FILE)" >&2; exit 2; }

require-compose: require-docker require-env
	@test -f "$(BASE_COMPOSE)" || { printf "\033[31m✗\033[0m %s not found\n" "$(BASE_COMPOSE)" >&2; exit 2; }
