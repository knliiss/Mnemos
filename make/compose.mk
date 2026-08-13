.PHONY: up stop restart down logs pull compose-config

up: require-compose
	$(call title,stack,UP)
	@$(DC) up -d --no-build --no-recreate
	$(call success,stack,running)

stop: require-compose
	$(call title,stack,STOP)
	@$(DC) stop
	$(call success,stack,stopped)

restart: require-gradle require-compose
	$(call title,stack,RESTART)
	$(call run_step,Gradle build,$(GRADLEW) build --console=plain)
	$(call run_step,Docker images,$(DC) build --quiet $(APP_SERVICE_NAMES))
	$(call run_step,Recreate stack,$(DC) up -d --force-recreate --no-build)
	$(call success,stack,restarted with current changes)

down: require-compose
	$(call title,stack,DOWN)
	@$(DC) down --remove-orphans
	$(call success,stack,removed)

logs: require-compose
	@$(DC) logs -f --tail=$(LOG_TAIL)

pull: require-compose
	$(call title,stack,PULL)
	@$(DC) pull
	$(call success,stack,pulled)

compose-config: require-compose
	@$(DC) config

%-up: require-compose
	@target="$*"; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "UP"; \
	$(DC) up -d --no-build --no-recreate "$$target" >/dev/null; \
	printf "$(GREEN)✓$(RESET) $(BOLD)%s$(RESET) $(DIM)%s$(RESET)\n" "$*" "running"

%-stop: require-compose
	@target="$*"; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "STOP"; \
	$(DC) stop "$$target" >/dev/null; \
	printf "$(GREEN)✓$(RESET) $(BOLD)%s$(RESET) $(DIM)%s$(RESET)\n" "$*" "stopped"

%-restart: require-compose
	@target="$*"; \
	is_app=0; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; is_app=1; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "RESTART"; \
	run_quiet() { \
		label="$$1"; shift; \
		tmp=$$(mktemp); started=$$(date +%s); \
		printf "  $(BLUE)›$(RESET) %-18s" "$$label"; \
		if "$$@" >"$$tmp" 2>&1; then \
			finished=$$(date +%s); elapsed=$$((finished - started)); \
			printf "$(GREEN)done$(RESET) $(DIM)%ss$(RESET)\n" "$$elapsed"; \
			rm -f "$$tmp"; \
		else \
			status=$$?; printf "$(RED)failed$(RESET)\n\n"; cat "$$tmp"; rm -f "$$tmp"; return $$status; \
		fi; \
	}; \
	if [ "$$is_app" = "1" ]; then \
		run_quiet "Gradle" $(GRADLEW) :apps:$$target:build --console=plain || exit $$?; \
		run_quiet "Docker image" $(DC) build --quiet "$$target" || exit $$?; \
	fi; \
	run_quiet "Recreate" $(DC) up -d --no-deps --force-recreate --no-build "$$target" || exit $$?; \
	printf "$(GREEN)✓$(RESET) $(BOLD)%s$(RESET) $(DIM)%s$(RESET)\n" "$*" "running with current changes"

%-remove: require-compose
	@target="$*"; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "REMOVE"; \
	$(DC) rm -f -s "$$target" >/dev/null; \
	printf "$(GREEN)✓$(RESET) $(BOLD)%s$(RESET) $(DIM)%s$(RESET)\n" "$*" "removed"

%-logs: require-compose
	@target="$*"; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	$(DC) logs -f --tail=$(LOG_TAIL) "$$target"

%-shell: require-compose
	@target="$*"; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	$(DC) exec "$$target" sh

%-pull: require-compose
	@target="$*"; \
	if [ -d "apps/$$target-service" ]; then target="$$target-service"; fi; \
	if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$*" >&2; exit 2; \
	fi; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "PULL"; \
	$(DC) pull "$$target" >/dev/null; \
	printf "$(GREEN)✓$(RESET) $(BOLD)%s$(RESET) $(DIM)%s$(RESET)\n" "$*" "pulled"
