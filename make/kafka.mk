.PHONY: kafka-init-build kafka-topics kafka-groups

KAFKA_ADMIN_CONFIG := /tmp/kafka-health.properties

kafka-init-build: require-compose
	$(call title,kafka-init,BUILD)
	$(call run_step,Image,$(DC) build --quiet kafka-init)
	$(call success,kafka-init,build complete)

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
