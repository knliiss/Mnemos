# =============================================================================
# Terminal formatting
# =============================================================================

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

define info
	@printf "$(CYAN)→ %s$(RESET)\n" "$(1)"
endef

define success
	@printf "$(GREEN)✓ %s$(RESET)\n" "$(1)"
endef

define warning
	@printf "$(YELLOW)! %s$(RESET)\n" "$(1)"
endef

define failure
	@printf "$(RED)✗ %s$(RESET)\n" "$(1)"
endef
