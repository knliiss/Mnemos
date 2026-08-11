.PHONY: help build test check clean dependencies

help:
	@printf "$(BOLD)Mnemos$(RESET)\n\n"

	@printf "Project:\n"
	@printf "  make build [SERVICE=reward]       Build project/service and Docker image\n"
	@printf "  make test [SERVICE=reward]        Run tests only\n"
	@printf "  make check [SERVICE=reward]       Run verification only\n"
	@printf "  make clean [SERVICE=reward]       Remove Gradle output only\n"
	@printf "  make dependencies [SERVICE=...]   Print Gradle dependencies\n\n"

	@printf "Containers:\n"
	@printf "  make up [SERVICE=reward|TARGET=kafka]      Start without build or recreation\n"
	@printf "  make stop [SERVICE=reward|TARGET=kafka]    Stop only\n"
	@printf "  make restart SERVICE=reward                Build and recreate service\n"
	@printf "  make remove SERVICE=reward                 Remove service container\n"
	@printf "  make down                                  Remove complete stack\n"
	@printf "  make logs [SERVICE=reward|TARGET=kafka]    Follow logs\n"
	@printf "  make ps                                    Show container state\n"
	@printf "  make shell SERVICE=reward                  Open service shell\n"
	@printf "  make config                                Print merged Compose config\n"
	@printf "  make pull [SERVICE=reward|TARGET=kafka]    Pull images\n\n"

	@printf "Kafka:\n"
	@printf "  make kafka-topics\n"
	@printf "  make kafka-groups\n"
	@printf "  make kafka-topic-create TOPIC=x\n"
	@printf "  make kafka-topic-delete TOPIC=x\n"
	@printf "  make kafka-topic-describe TOPIC=x\n\n"

	@printf "Postgres:\n"
	@printf "  make psql\n"
	@printf "  make psql-keycloak\n\n"

	@printf "Keycloak:\n"
	@printf "  make keycloak-import\n"

build: validate
	$(call step,Building $(if $(SERVICE_NAME),$(SERVICE_NAME),complete project))
	@$(GRADLEW) $(if $(SERVICE_NAME),$(APP_MODULE):build,build)

	$(call step,Building $(if $(SERVICE_NAME),$(SERVICE_NAME),all) Docker image$(if $(SERVICE_NAME),,s))
	@$(DC) build $(SERVICE_NAME)

	$(call done,Built $(if $(SERVICE_NAME),$(SERVICE_NAME),complete project))

test:
	$(call step,Testing $(if $(SERVICE_NAME),$(SERVICE_NAME),complete project))
	@$(GRADLEW) $(if $(SERVICE_NAME),$(APP_MODULE):test,test)

	$(call done,Tests completed)

check:
	$(call step,Checking $(if $(SERVICE_NAME),$(SERVICE_NAME),complete project))
	@$(GRADLEW) $(if $(SERVICE_NAME),$(APP_MODULE):check,check)

	$(call done,Checks completed)

clean:
	$(call step,Cleaning $(if $(SERVICE_NAME),$(SERVICE_NAME),complete project))
	@$(GRADLEW) $(if $(SERVICE_NAME),$(APP_MODULE):clean,clean)

	$(call done,Clean completed)

dependencies:
	@$(GRADLEW) $(if $(SERVICE_NAME),$(APP_MODULE):dependencies,dependencies)