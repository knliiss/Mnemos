.PHONY: kafka-topics kafka-groups kafka-topic-create kafka-topic-delete kafka-topic-describe
kafka-topics: validate
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:29092 --list
kafka-groups: validate
	@$(DC) exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka:29092 --list
kafka-topic-create: validate require-topic
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:29092 --create --if-not-exists --topic "$(TOPIC)" --partitions $(PARTITIONS) --replication-factor $(REPLICATION_FACTOR)
kafka-topic-delete: validate require-topic
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:29092 --delete --topic "$(TOPIC)"
kafka-topic-describe: validate require-topic
	@$(DC) exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:29092 --describe --topic "$(TOPIC)"
