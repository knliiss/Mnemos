.PHONY: image up stop restart remove down logs ps shell config pull
image: validate require-container-target
	$(call step,Building Docker image for $(CONTAINER_TARGET))
	@$(DC) build $(CONTAINER_TARGET)

up: validate
	$(call step,Starting $(if $(CONTAINER_TARGET),$(CONTAINER_TARGET),stack) without build or recreation)
	@$(DC) up -d --no-build --no-recreate $(CONTAINER_TARGET)

stop: validate
	$(call step,Stopping $(if $(CONTAINER_TARGET),$(CONTAINER_TARGET),stack))
	@$(DC) stop $(CONTAINER_TARGET)

restart: validate require-container-target
	$(call step,Restarting $(CONTAINER_TARGET))
	@$(DC) restart $(CONTAINER_TARGET)

remove: validate require-container-target
	$(call step,Removing $(CONTAINER_TARGET) container)
	@$(DC) rm -f -s $(CONTAINER_TARGET)

down: validate
	$(call step,Removing complete stack containers and network)
	@$(DC) down --remove-orphans

logs: validate
	@$(DC) logs -f --tail=$(LOG_TAIL) $(CONTAINER_TARGET)

ps: validate
	@$(DC) ps -a

shell: validate require-container-target
	@$(DC) exec $(CONTAINER_TARGET) sh

config: validate
	@$(DC) config

pull: validate
	@$(DC) pull $(CONTAINER_TARGET)
