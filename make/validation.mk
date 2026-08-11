.PHONY: validate require-service require-container-target require-topic
validate:
	@test -f "$(ENV_FILE)" || { echo "Missing $(ENV_FILE). Copy .env.example and configure it."; exit 1; }
	@command -v docker >/dev/null || { echo "docker is required"; exit 1; }
	@test -x "$(GRADLEW)" || { echo "$(GRADLEW) is not executable"; exit 1; }

require-service:
	@test -n "$(SERVICE)" || { echo "SERVICE is required. Example: make up SERVICE=reward"; exit 2; }

require-container-target:
	@test -n "$(CONTAINER_TARGET)" || { echo "SERVICE or TARGET is required. Examples: SERVICE=reward or TARGET=kafka"; exit 2; }

require-topic:
	@test -n "$(TOPIC)" || { echo "TOPIC is required."; exit 2; }
