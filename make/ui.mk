ifeq ($(NO_COLOR),1)
RESET :=
BOLD :=
DIM :=
RED :=
GREEN :=
YELLOW :=
BLUE :=
MAGENTA :=
CYAN :=
else
RESET := \033[0m
BOLD := \033[1m
DIM := \033[2m
RED := \033[31m
GREEN := \033[32m
YELLOW := \033[33m
BLUE := \033[34m
MAGENTA := \033[35m
CYAN := \033[36m
endif

define title
	@printf "$(BOLD)$(MAGENTA)◆$(RESET) $(BOLD)%-18s$(RESET) $(CYAN)%s$(RESET)\n" "$(1)" "$(2)"
endef

define run_step
	@tmp=$$(mktemp); \
	started=$$(date +%s); \
	printf "  $(BLUE)›$(RESET) %-18s" "$(1)"; \
	if $(2) >"$$tmp" 2>&1; then \
		finished=$$(date +%s); \
		elapsed=$$((finished - started)); \
		printf "$(GREEN)done$(RESET) $(DIM)%ss$(RESET)\n" "$$elapsed"; \
		rm -f "$$tmp"; \
	else \
		status=$$?; \
		printf "$(RED)failed$(RESET)\n"; \
		printf "\n$(DIM)────────────────────────────────────────────────────────$(RESET)\n"; \
		cat "$$tmp"; \
		printf "$(DIM)────────────────────────────────────────────────────────$(RESET)\n"; \
		rm -f "$$tmp"; \
		exit $$status; \
	fi
endef

define success
	@printf "$(GREEN)✓$(RESET) $(BOLD)%s$(RESET) $(DIM)%s$(RESET)\n" "$(1)" "$(2)"
endef
