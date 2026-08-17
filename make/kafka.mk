.PHONY: kafka-init-build kafka-topics kafka-groups


kafka-init-build: require-compose
	$(call title,kafka-init,BUILD)
	$(call run_step,Image,$(DC) build --quiet kafka-init)
	$(call success,kafka-init,build complete)

kafka-topics: require-compose
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
		--list

kafka-groups: require-compose
	@$(DC) exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
		--bootstrap-server kafka:29092 \
		--list

kafka-create-%: require-compose
	$(call title,kafka,CREATE TOPIC)
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
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
		--delete \
		--topic "$*"
	$(call success,$*,deleted)

kafka-describe-%: require-compose
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh \
		--bootstrap-server kafka:29092 \
		--describe \
		--topic "$*"
