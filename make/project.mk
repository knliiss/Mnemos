# =============================================================================
##@ Gradle project
# =============================================================================

.PHONY: build check test clean dependencies

build: ## Build the complete Gradle project
	@$(GRADLEW) build

check: ## Run all Gradle verification tasks
	@$(GRADLEW) check

test: ## Run all unit tests
	@$(GRADLEW) test

clean: ## Remove all Gradle build output
	@$(GRADLEW) clean

dependencies: ## Print Gradle dependency information
	@$(GRADLEW) dependencies
