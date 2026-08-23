.PHONY: kafka-init-build kafka-topics kafka-groups kafka-message-auth-sync kafka-message-auth-verify kafka-message-auth-runtime-verify

KAFKA_ADMIN_CONFIG := /tmp/kafka-health.properties
KAFKA_MESSAGE_AUTH_SYNC := scripts/generate-kafka-message-keys.sh
KAFKA_MESSAGE_AUTH_VERIFY := scripts/verify-kafka-message-auth.sh
KAFKA_MESSAGE_AUTH_RUNTIME_VERIFY := scripts/verify-kafka-message-auth-runtime.sh

kafka-init-build: require-compose
	$(call title,kafka-init,BUILD)
	$(call run_step,Image,$(DC) build --quiet kafka-init)
	$(call success,kafka-init,build complete)

kafka-message-auth-sync:
	$(call title,kafka-message-auth,SYNC)
	@$(KAFKA_MESSAGE_AUTH_SYNC) $(ENV_FILE)

kafka-message-auth-verify:
	@$(KAFKA_MESSAGE_AUTH_VERIFY) $(ENV_FILE)

kafka-message-auth-runtime-verify: kafka-message-auth-verify
	@$(KAFKA_MESSAGE_AUTH_RUNTIME_VERIFY) $(ENV_FILE)

kafka-topics: require-compose
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
		--command-config $(KAFKA_ADMIN_CONFIG) \
		--list

kafka-groups: require-compose
	@$(DC) exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
		--bootstrap-server kafka:29092 \
		--command-config $(KAFKA_ADMIN_CONFIG) \
		--list

kafka-create-%: require-compose
	$(call title,kafka,CREATE TOPIC)
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
		--command-config $(KAFKA_ADMIN_CONFIG) \
		--create \
		--if-not-exists \
		--topic "$*" \
		--partitions $(KAFKA_PARTITIONS) \
		--replication-factor $(KAFKA_REPLICATION_FACTOR)
	$(call success,$*,created)

kafka-delete-%: require-compose
	$(call title,kafka,DELETE TOPIC)
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
		--command-config $(KAFKA_ADMIN_CONFIG) \
		--delete \
		--topic "$*"
	$(call success,$*,deleted)

kafka-describe-%: require-compose
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
		--command-config $(KAFKA_ADMIN_CONFIG) \
		--describe \
		--topic "$*"
