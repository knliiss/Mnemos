.PHONY: help build test check clean deps status

help:
	@printf "\033[1mMnemos\033[0m development commands\n\n"
	@printf "\033[1mProject\033[0m\n"
	@printf "  \033[36m%-28s\033[0m %s\n" "make build" "Build project and Docker images"
	@printf "  \033[36m%-28s\033[0m %s\n" "make test" "Run all tests"
	@printf "  \033[36m%-28s\033[0m %s\n" "make check" "Run all checks"
	@printf "  \033[36m%-28s\033[0m %s\n" "make clean" "Clean Gradle outputs"
	@printf "\n\033[1mStack\033[0m\n"
	@printf "  \033[36m%-28s\033[0m %s\n" "make up" "Start stopped services only"
	@printf "  \033[36m%-28s\033[0m %s\n" "make stop" "Stop running services"
	@printf "  \033[36m%-28s\033[0m %s\n" "make restart" "Build images and recreate services"
	@printf "  \033[36m%-28s\033[0m %s\n" "make down" "Remove the stack"
	@printf "  \033[36m%-28s\033[0m %s\n" "make logs" "Follow stack logs"
	@printf "  \033[36m%-28s\033[0m %s\n" "make status" "Show service status"
	@printf "\n\033[1mApplication commands\033[0m\n"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-build" "Build service and image"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-test" "Run service tests"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-check" "Run service checks"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-up" "Start service only"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-restart" "Build image and recreate service"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-stop" "Stop service"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-logs" "Follow service logs"
	@printf "  \033[36m%-28s\033[0m %s\n" "make <service>-shell" "Open service shell"
	@printf "\n\033[1mInfrastructure\033[0m\n"
	@printf "  \033[36m%-28s\033[0m %s\n" "make kafka-topics" "List Kafka topics"
	@printf "  \033[36m%-28s\033[0m %s\n" "make kafka-groups" "List Kafka consumer groups"
	@printf "  \033[36m%-28s\033[0m %s\n" "make kafka-create-<topic>" "Create Kafka topic"
	@printf "  \033[36m%-28s\033[0m %s\n" "make app-postgres-shell" "Open application PostgreSQL shell"
	@printf "  \033[36m%-28s\033[0m %s\n" "make keycloak-import" "Import Keycloak configuration"
	@printf "  \033[36m%-28s\033[0m %s\n" "make completion-install" "Install zsh completion"
	@printf "\n\033[1mServices\033[0m  %s\n" "$(APP_NAMES)"

build: require-gradle require-compose
	$(call title,project,BUILD)
	$(call run_step,Gradle,$(GRADLEW) build --console=plain)
	$(call run_step,Images,$(DC) build --quiet $(APP_SERVICE_NAMES))
	$(call success,project,build complete)

test: require-gradle
	$(call title,project,TEST)
	@$(GRADLEW) test --console=plain

check: require-gradle
	$(call title,project,CHECK)
	@$(GRADLEW) check --console=plain

clean: require-gradle
	$(call title,project,CLEAN)
	@$(GRADLEW) clean --console=plain
	$(call success,project,clean complete)

deps: require-gradle
	@$(GRADLEW) dependencies --console=plain

status: require-compose
	@$(DC) ps -a

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
