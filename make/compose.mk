.PHONY: up stop restart down logs pull status compose-config

resolve_compose_service = $(if $(filter $(1),$(APP_NAMES)),$(1)-service,$(1))

define verify_containers
	@sleep 2; \
	failed=""; \
	for service in $(1); do \
		is_one_shot=0; \
		case " $(ONE_SHOT_SERVICE_NAMES) " in \
			*" $$service "*) is_one_shot=1 ;; \
		esac; \
		cid="$$($(DC) ps -aq "$$service" 2>/dev/null | head -n 1)"; \
		if [ -z "$$cid" ]; then \
			printf "$(RED)✗$(RESET) %s container is missing\n" "$$service" >&2; \
			failed="$$failed $$service"; \
			continue; \
		fi; \
		state="$$(docker inspect -f '{{.State.Status}}' "$$cid" 2>/dev/null || true)"; \
		exit_code="$$(docker inspect -f '{{.State.ExitCode}}' "$$cid" 2>/dev/null || true)"; \
		health="$$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$$cid" 2>/dev/null || true)"; \
		if [ "$$health" = "unhealthy" ]; then \
			printf "$(RED)✗$(RESET) %s is unhealthy\n" "$$service" >&2; \
			failed="$$failed $$service"; \
		elif [ "$$state" = "running" ]; then \
			:; \
		elif [ "$$is_one_shot" -eq 1 ] && [ "$$state" = "exited" ] && [ "$$exit_code" = "0" ]; then \
			:; \
		else \
			printf "$(RED)✗$(RESET) %s is %s (exit=%s, health=%s)\n" "$$service" "$$state" "$$exit_code" "$$health" >&2; \
			failed="$$failed $$service"; \
		fi; \
	done; \
	if [ -n "$$failed" ]; then \
		printf "\n$(DIM)Recent logs for failed services:$(RESET)\n" >&2; \
		for service in $$failed; do \
			printf "\n$(DIM)----- %s -----$(RESET)\n" "$$service" >&2; \
			$(DC) logs --tail=$(LOG_TAIL) "$$service" >&2 || true; \
		done; \
		exit 1; \
	fi
endef

define require_known_compose_service
	@if ! printf '%s\n' "$(COMPOSE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$(call resolve_compose_service,$(1))"; then \
		printf "$(RED)✗$(RESET) Unknown compose service: %s\n" "$(1)" >&2; \
		exit 2; \
	fi
endef

up: require-compose
	$(call title,stack,UP)
	@$(DC) up -d --no-build --no-recreate
	$(call verify_containers,$(COMPOSE_SERVICE_NAMES))
	$(call success,stack,running)

stop: require-compose
	$(call title,stack,STOP)
	@$(DC) stop
	$(call success,stack,stopped)

restart: require-gradle require-compose
	$(call title,stack,RESTART)
	$(call run_step,Gradle build,$(GRADLEW) build --console=plain)
	$(call run_step,Docker images,$(DC) build --quiet $(BUILDABLE_SERVICE_NAMES))
	$(call run_step,Recreate stack,$(DC) up -d --force-recreate --no-build)
	$(call verify_containers,$(COMPOSE_SERVICE_NAMES))
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

status: require-compose
	@$(DC) ps -a
	$(call verify_containers,$(COMPOSE_SERVICE_NAMES))

compose-config: require-compose
	@$(DC) config

%-up: require-compose
	$(call require_known_compose_service,$*)
	@target="$(call resolve_compose_service,$*)"; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "UP"; \
	$(DC) up -d --no-build --no-recreate "$$target" >/dev/null
	$(call verify_containers,$(call resolve_compose_service,$*))
	$(call success,$*,running)

%-stop: require-compose
	$(call require_known_compose_service,$*)
	@target="$(call resolve_compose_service,$*)"; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "STOP"; \
	$(DC) stop "$$target" >/dev/null
	$(call success,$*,stopped)

%-restart: require-compose
	$(call require_known_compose_service,$*)
	@target="$(call resolve_compose_service,$*)"; \
	is_app=0; \
	is_buildable=0; \
	if printf '%s\n' "$(APP_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then is_app=1; fi; \
	if printf '%s\n' "$(BUILDABLE_SERVICE_NAMES)" | tr ' ' '\n' | grep -qx "$$target"; then is_buildable=1; fi; \
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
	fi; \
	if [ "$$is_buildable" = "1" ]; then \
		run_quiet "Docker image" $(DC) build --quiet "$$target" || exit $$?; \
	fi; \
	run_quiet "Recreate" $(DC) up -d --no-deps --force-recreate --no-build "$$target" || exit $$?
	$(call verify_containers,$(call resolve_compose_service,$*))
	$(call success,$*,running with current changes)

%-status: require-compose
	$(call require_known_compose_service,$*)
	@$(DC) ps -a "$(call resolve_compose_service,$*)"
	$(call verify_containers,$(call resolve_compose_service,$*))

%-remove: require-compose
	$(call require_known_compose_service,$*)
	@target="$(call resolve_compose_service,$*)"; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "REMOVE"; \
	$(DC) rm -f -s "$$target" >/dev/null
	$(call success,$*,removed)

%-logs: require-compose
	$(call require_known_compose_service,$*)
	@$(DC) logs -f --tail=$(LOG_TAIL) "$(call resolve_compose_service,$*)"

%-shell: require-compose
	$(call require_known_compose_service,$*)
	@$(DC) exec "$(call resolve_compose_service,$*)" sh

%-pull: require-compose
	$(call require_known_compose_service,$*)
	@target="$(call resolve_compose_service,$*)"; \
	printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$*" "PULL"; \
	$(DC) pull "$$target" >/dev/null
	$(call success,$*,pulled)
