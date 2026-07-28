# =============================================================================
##@ Kafka
# =============================================================================

.PHONY: kafka-up kafka-stop kafka-restart kafka-logs kafka-topics kafka-groups \
	kafka-topic-create kafka-topic-describe kafka-topic-delete kafka-api-versions

kafka-up: check-env ## Start Kafka
	@$(DC_INFRA) up -d --wait --wait-timeout $(WAIT_TIMEOUT) $(KAFKA_SERVICE)
	@$(MAKE) --no-print-directory status

kafka-stop: check-env ## Stop Kafka
	@$(DC_INFRA) stop $(KAFKA_SERVICE)

kafka-restart: check-env ## Recreate Kafka
	@$(DC_INFRA) up -d --force-recreate --wait --wait-timeout $(WAIT_TIMEOUT) $(KAFKA_SERVICE)
	@$(MAKE) --no-print-directory status

kafka-logs: check-env ## Follow Kafka logs
	@$(DC_INFRA) logs -f --tail=$(LOG_TAIL) $(KAFKA_SERVICE)

kafka-topics: check-env ## List Kafka topics
	@$(DC_INFRA) exec -T $(KAFKA_SERVICE) \
		$(KAFKA_TOOLS_DIR)/kafka-topics.sh \
		--bootstrap-server $(KAFKA_BOOTSTRAP_INTERNAL) \
		--list

kafka-groups: check-env ## List Kafka consumer groups
	@$(DC_INFRA) exec -T $(KAFKA_SERVICE) \
		$(KAFKA_TOOLS_DIR)/kafka-consumer-groups.sh \
		--bootstrap-server $(KAFKA_BOOTSTRAP_INTERNAL) \
		--list

kafka-topic-create: check-env ## Create TOPIC; optional PARTITIONS and REPLICATION_FACTOR
	@if [ -z "$(TOPIC)" ]; then \
		printf "$(RED)TOPIC is required. Example: make kafka-topic-create TOPIC=mnemos.account.events.v1$(RESET)\n"; \
		exit 1; \
	fi
	@$(DC_INFRA) exec -T $(KAFKA_SERVICE) \
		$(KAFKA_TOOLS_DIR)/kafka-topics.sh \
		--bootstrap-server $(KAFKA_BOOTSTRAP_INTERNAL) \
		--create \
		--if-not-exists \
		--topic "$(TOPIC)" \
		--partitions "$(PARTITIONS)" \
		--replication-factor "$(REPLICATION_FACTOR)"

kafka-topic-describe: check-env ## Describe TOPIC
	@if [ -z "$(TOPIC)" ]; then \
		printf "$(RED)TOPIC is required. Example: make kafka-topic-describe TOPIC=mnemos.account.events.v1$(RESET)\n"; \
		exit 1; \
	fi
	@$(DC_INFRA) exec -T $(KAFKA_SERVICE) \
		$(KAFKA_TOOLS_DIR)/kafka-topics.sh \
		--bootstrap-server $(KAFKA_BOOTSTRAP_INTERNAL) \
		--describe \
		--topic "$(TOPIC)"

kafka-topic-delete: check-env ## Delete TOPIC; requires CONFIRM=1
	@if [ -z "$(TOPIC)" ]; then \
		printf "$(RED)TOPIC is required$(RESET)\n"; \
		exit 1; \
	fi
	@if [ "$(CONFIRM)" != "1" ]; then \
		printf "$(RED)Refusing topic deletion. Run: make kafka-topic-delete TOPIC=$(TOPIC) CONFIRM=1$(RESET)\n"; \
		exit 1; \
	fi
	@$(DC_INFRA) exec -T $(KAFKA_SERVICE) \
		$(KAFKA_TOOLS_DIR)/kafka-topics.sh \
		--bootstrap-server $(KAFKA_BOOTSTRAP_INTERNAL) \
		--delete \
		--topic "$(TOPIC)"

kafka-api-versions: check-env ## Show broker API versions
	@$(DC_INFRA) exec -T $(KAFKA_SERVICE) \
		$(KAFKA_TOOLS_DIR)/kafka-broker-api-versions.sh \
		--bootstrap-server $(KAFKA_BOOTSTRAP_INTERNAL)
