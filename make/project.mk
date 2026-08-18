.PHONY: help build test check ktlintFormat clean deps

help:
	@printf "\033[1mMnemos\033[0m development commands\n\n"
	@printf "\033[1mProject\033[0m\n"
	@printf "  \033[36m%-32s\033[0m %s\n" "make build" "Build project and Docker images"
	@printf "  \033[36m%-32s\033[0m %s\n" "make test" "Run all tests"
	@printf "  \033[36m%-32s\033[0m %s\n" "make check" "Run all checks"
	@printf "  \033[36m%-32s\033[0m %s\n" "make ktlintFormat" "Format Kotlin sources with ktlint"
	@printf "  \033[36m%-32s\033[0m %s\n" "make clean" "Clean Gradle outputs"
	@printf "  \033[36m%-32s\033[0m %s\n" "make deps" "Show root project dependencies"
	@printf "  \033[36m%-32s\033[0m %s\n" "make make-verify" "Validate Make target wiring without executing targets"
	@printf "\n\033[1mStack\033[0m\n"
	@printf "  \033[36m%-32s\033[0m %s\n" "make up" "Start stopped services only"
	@printf "  \033[36m%-32s\033[0m %s\n" "make stop" "Stop running services"
	@printf "  \033[36m%-32s\033[0m %s\n" "make restart" "Build images and recreate services"
	@printf "  \033[36m%-32s\033[0m %s\n" "make down" "Remove the stack"
	@printf "  \033[36m%-32s\033[0m %s\n" "make logs" "Follow stack logs"
	@printf "  \033[36m%-32s\033[0m %s\n" "make status" "Show service status and validate container state"
	@printf "  \033[36m%-32s\033[0m %s\n" "make pull" "Pull external infrastructure images"
	@printf "  \033[36m%-32s\033[0m %s\n" "make compose-config" "Show merged Docker Compose configuration"
	@printf "\n\033[1mApplication commands\033[0m\n"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-build" "Build service and image"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-test" "Run service tests"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-check" "Run service checks"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-clean" "Clean service Gradle outputs"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-deps" "Show service dependencies"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-up" "Start service only"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-restart" "Build image and recreate service"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-status" "Show and validate service status"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-stop" "Stop service"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-remove" "Remove service container"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-logs" "Follow service logs"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <service>-shell" "Open service shell"
	@printf "\n\033[1mInfrastructure\033[0m\n"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <compose-service>-<action>" "Use up/stop/restart/status/remove/logs"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <running-service>-shell" "Open shell for non-one-shot compose service"
	@printf "  \033[36m%-32s\033[0m %s\n" "make <external-service>-pull" "Pull external infrastructure image"
	@printf "  \033[36m%-32s\033[0m %s\n" "make kafka-init-build" "Build Kafka bootstrap image"
	@printf "  \033[36m%-32s\033[0m %s\n" "make kafka-topics" "List Kafka topics"
	@printf "  \033[36m%-32s\033[0m %s\n" "make kafka-groups" "List Kafka consumer groups"
	@printf "  \033[36m%-32s\033[0m %s\n" "make kafka-create-<topic>" "Create Kafka topic"
	@printf "  \033[36m%-32s\033[0m %s\n" "make kafka-delete-<topic>" "Delete Kafka topic"
	@printf "  \033[36m%-32s\033[0m %s\n" "make kafka-describe-<topic>" "Describe Kafka topic"
	@printf "  \033[36m%-32s\033[0m %s\n" "make app-postgres-shell" "Open application PostgreSQL shell"
	@printf "  \033[36m%-32s\033[0m %s\n" "make keycloak-postgres-shell" "Open Keycloak PostgreSQL shell"
	@printf "  \033[36m%-32s\033[0m %s\n" "make keycloak-restart" "Rebuild authenticator and recreate Keycloak"
	@printf "  \033[36m%-32s\033[0m %s\n" "make keycloak-import" "Import Keycloak configuration"
	@printf "  \033[36m%-32s\033[0m %s\n" "make completion-install" "Install zsh completion"
	@printf "  \033[36m%-32s\033[0m %s\n" "make completion-uninstall" "Remove zsh completion"
	@printf "\n\033[1mApplications\033[0m  %s\n" "$(APP_NAMES)"
	@printf "\033[1mCompose services\033[0m  %s\n" "$(COMPOSE_SERVICE_NAMES)"

build: require-gradle require-compose
	$(call title,project,BUILD)
	$(call run_step,Gradle,$(GRADLEW) build --console=plain)
	$(call run_step,Images,$(DC) build --quiet $(BUILDABLE_SERVICE_NAMES))
	$(call success,project,build complete)

test: require-gradle
	$(call title,project,TEST)
	@$(GRADLEW) test --console=plain

check: require-gradle
	$(call title,project,CHECK)
	@$(GRADLEW) check --console=plain

ktlintFormat: require-gradle
	$(call title,project,KTLINT FORMAT)
	@$(GRADLEW) ktlintFormat --console=plain

clean: require-gradle
	$(call title,project,CLEAN)
	@$(GRADLEW) clean --console=plain
	$(call success,project,clean complete)

deps: require-gradle
	@$(GRADLEW) dependencies --console=plain

%-build: require-gradle require-compose
	@if [ ! -d "apps/$*-service" ]; then printf "\033[31m✗\033[0m Unknown application: %s\n" "$*" >&2; exit 2; fi
	$(call title,$*,BUILD)
	$(call run_step,Gradle,$(GRADLEW) :apps:$*-service:build --console=plain)
	$(call run_step,Image,$(DC) build --quiet $*-service)
	$(call success,$*,build complete)

%-test: require-gradle
	@if [ ! -d "apps/$*-service" ]; then printf "\033[31m✗\033[0m Unknown application: %s\n" "$*" >&2; exit 2; fi
	$(call title,$*,TEST)
	@$(GRADLEW) :apps:$*-service:test --console=plain

%-check: require-gradle
	@if [ ! -d "apps/$*-service" ]; then printf "\033[31m✗\033[0m Unknown application: %s\n" "$*" >&2; exit 2; fi
	$(call title,$*,CHECK)
	@$(GRADLEW) :apps:$*-service:check --console=plain

%-clean: require-gradle
	@if [ ! -d "apps/$*-service" ]; then printf "\033[31m✗\033[0m Unknown application: %s\n" "$*" >&2; exit 2; fi
	$(call title,$*,CLEAN)
	@$(GRADLEW) :apps:$*-service:clean --console=plain
	$(call success,$*,clean complete)

%-deps: require-gradle
	@if [ ! -d "apps/$*-service" ]; then printf "\033[31m✗\033[0m Unknown application: %s\n" "$*" >&2; exit 2; fi
	@$(GRADLEW) :apps:$*-service:dependencies --console=plain
