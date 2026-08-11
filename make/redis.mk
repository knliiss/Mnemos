# =============================================================================
##@ Redis
# =============================================================================

.PHONY: redis-up redis-stop redis-restart redis-logs redis-status redis-cli

redis-up: check-env ## Start Redis
	@$(DC_REDIS) up -d --wait --wait-timeout $(WAIT_TIMEOUT) $(REDIS_SERVICE)
	@$(MAKE) --no-print-directory status

redis-stop: check-env ## Stop Redis
	@$(DC_REDIS) stop $(REDIS_SERVICE)

redis-restart: check-env ## Recreate Redis
	@$(DC_REDIS) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) $(REDIS_SERVICE)
	@$(MAKE) --no-print-directory status

redis-logs: check-env ## Follow Redis logs
	@$(DC_REDIS) logs -f --tail=$(LOG_TAIL) $(REDIS_SERVICE)

redis-status: check-env ## Ping Redis
	@$(DC_REDIS) exec -T $(REDIS_SERVICE) \
		sh -lc 'REDISCLI_AUTH="$$REDIS_PASSWORD" redis-cli ping'

redis-cli: check-env ## Open Redis CLI
	@$(DC_REDIS) exec $(REDIS_SERVICE) \
		sh -lc 'REDISCLI_AUTH="$$REDIS_PASSWORD" redis-cli'