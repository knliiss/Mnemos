ifeq ($(NO_COLOR),1)
RESET :=
BOLD :=
CYAN :=
GREEN :=
YELLOW :=
else
RESET := \033[0m
BOLD := \033[1m
CYAN := \033[36m
GREEN := \033[32m
YELLOW := \033[33m
endif

define step
	@printf "$(CYAN)→$(RESET) %s\n" "$(1)"
endef

define done
	@printf "$(GREEN)✓$(RESET) %s\n" "$(1)"
endef
