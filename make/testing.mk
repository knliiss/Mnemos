MANUAL_TEST_ENV_PREPARE := scripts/prepare-manual-test-env.sh

.PHONY: test-env-prepare test-ready mini-app-install mini-app-verify mini-app-dev

test-env-prepare: require-env require-node
	$(call title,testing,ENVIRONMENT)
	$(call run_step,Runtime env,sh $(MANUAL_TEST_ENV_PREPARE) "$(ENV_FILE)" "$(MINI_APP_ENV_FILE)")
	$(call success,testing,manual test environment prepared)

mini-app-install: require-node
	$(call title,mini-app,INSTALL)
	$(call run_step,npm ci,cd "$(MINI_APP_DIR)" && npm ci)
	$(call success,mini-app,dependencies installed)

mini-app-verify: mini-app-install
	$(call title,mini-app,VERIFY)
	$(call run_step,npm verify,cd "$(MINI_APP_DIR)" && npm run verify)
	$(call success,mini-app,verification complete)

test-ready: test-env-prepare mini-app-verify restart
	$(call success,testing,backend stack and Mini App are ready for manual testing)

mini-app-dev: require-node
	@test -f "$(MINI_APP_ENV_FILE)" || { \
		printf "$(RED)✗$(RESET) %s is missing — run make test-ready first\n" "$(MINI_APP_ENV_FILE)" >&2; \
		exit 2; \
	}
	@test -x "$(MINI_APP_DIR)/node_modules/.bin/vite" || { \
		printf "$(RED)✗$(RESET) Mini App dependencies are missing — run make mini-app-install or make test-ready first\n" >&2; \
		exit 2; \
	}
	$(call title,mini-app,DEV)
	@cd "$(MINI_APP_DIR)" && npm run dev -- --host "$(MINI_APP_DEV_HOST)" --port "$(MINI_APP_DEV_PORT)"
