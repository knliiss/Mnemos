import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class KafkaBootstrap {
    private static final short REPLICATION_FACTOR = 1;

    private KafkaBootstrap() {
    }

    public static void main(String[] args) throws Exception {
        Properties properties = adminProperties();

        try (Admin admin = Admin.create(properties)) {
            createTopics(admin);
            createAcls(admin);
        }

        System.out.println("Kafka topics and ACLs are configured.");
    }

    private static Properties adminProperties() {
        String bootstrapServer = requiredEnvironment("KAFKA_BOOTSTRAP_SERVER");
        String adminPassword = requiredEnvironment("KAFKA_ADMIN_PASSWORD");

        Properties properties = new Properties();

        properties.put("bootstrap.servers", bootstrapServer);
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.mechanism", "PLAIN");
        properties.put(
                "sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"kafka_admin\" "
                        + "password=\"" + adminPassword + "\";"
        );

        properties.put("client.id", "mnemos-kafka-bootstrap");
        properties.put("request.timeout.ms", "30000");
        properties.put("default.api.timeout.ms", "60000");

        return properties;
    }

    private static void createTopics(Admin admin) throws Exception {
        Set<String> existingTopics = admin
                .listTopics()
                .names()
                .get();

        List<NewTopic> missingTopics = topics()
                .stream()
                .filter(topic -> !existingTopics.contains(topic.name()))
                .toList();

        if (missingTopics.isEmpty()) {
            System.out.println("Kafka topics are already configured.");
            return;
        }

        admin
                .createTopics(missingTopics)
                .all()
                .get();

        System.out.println("Created " + missingTopics.size() + " Kafka topics.");
    }

    private static List<NewTopic> topics() {
        List<NewTopic> topics = new ArrayList<>();

        topics.add(topic("mnemos.account.events.v1", 3));
        topics.add(topic("mnemos.account.events.v1.dlt", 3));

        topics.add(topic("mnemos.event.events.v1", 3));
        topics.add(topic("mnemos.event.events.v1.dlt", 3));

        topics.add(topic("mnemos.wallet.commands.v2", 3));
        topics.add(topic("mnemos.wallet.commands.v2.dlt", 3));

        topics.add(topic("mnemos.wallet.events.v2", 3));
        topics.add(topic("mnemos.wallet.events.v2.dlt", 3));

        topics.add(topic("mnemos.bet.events.v1", 3));
        topics.add(topic("mnemos.bet.events.v1.dlt", 3));

        topics.add(topic("mnemos.moderation.events.v1", 3));
        topics.add(topic("mnemos.moderation.events.v1.dlt", 3));

        topics.add(topic("mnemos.personalization.events.v1", 3));
        topics.add(topic("mnemos.personalization.events.v1.dlt", 3));

        topics.add(topic("mnemos.social.events.v1", 3));
        topics.add(topic("mnemos.social.events.v1.dlt", 3));

        topics.add(
                topic(
                        "mnemos.collector.ingress.v2",
                        1,
                        Map.of(
                                "cleanup.policy", "delete",
                                "retention.ms", "86400000",
                                "retention.bytes", "268435456",
                                "segment.ms", "300000",
                                "segment.bytes", "67108864",
                                "compression.type", "lz4"
                        )
                )
        );

        topics.add(
                topic(
                        "mnemos.collector.ingress.v2.dlt",
                        1,
                        Map.of(
                                "cleanup.policy", "delete",
                                "retention.ms", "604800000",
                                "retention.bytes", "67108864"
                        )
                )
        );

        return topics;
    }

    private static NewTopic topic(
            String name,
            int partitions
    ) {
        return new NewTopic(
                name,
                partitions,
                REPLICATION_FACTOR
        );
    }

    private static NewTopic topic(
            String name,
            int partitions,
            Map<String, String> configs
    ) {
        return new NewTopic(
                name,
                partitions,
                REPLICATION_FACTOR
        ).configs(configs);
    }

    private static void createAcls(Admin admin) throws Exception {
        Set<AclBinding> acls = new LinkedHashSet<>();

        allowTopicWrite(
                acls,
                "account_service",
                "mnemos.account.events.v1"
        );

        allowConsumer(
                acls,
                "wallet_service",
                "wallet-service",
                "mnemos.account.events.v1"
        );
        allowConsumer(
                acls,
                "wallet_service",
                "wallet-service",
                "mnemos.wallet.commands.v2"
        );
        allowTopicWrite(
                acls,
                "wallet_service",
                "mnemos.wallet.events.v2"
        );

        allowConsumer(
                acls,
                "event_service",
                "event-service",
                "mnemos.collector.ingress.v2"
        );
        allowTopicWrite(
                acls,
                "event_service",
                "mnemos.event.events.v1"
        );

        allowConsumer(
                acls,
                "reward_service",
                "reward-service",
                "mnemos.event.events.v1"
        );
        allowTopicWrite(
                acls,
                "reward_service",
                "mnemos.wallet.commands.v2"
        );

        allowConsumer(
                acls,
                "bet_service",
                "bet-service",
                "mnemos.event.events.v1"
        );
        allowConsumer(
                acls,
                "bet_service",
                "bet-service",
                "mnemos.wallet.events.v2"
        );
        allowTopicWrite(
                acls,
                "bet_service",
                "mnemos.bet.events.v1"
        );
        allowTopicWrite(
                acls,
                "bet_service",
                "mnemos.wallet.commands.v2"
        );

        allowTopicWrite(
                acls,
                "moderation_service",
                "mnemos.moderation.events.v1"
        );

        allowConsumer(
                acls,
                "gateway_service",
                "gateway-service",
                "mnemos.moderation.events.v1"
        );

        allowConsumer(
                acls,
                "notification_service",
                "notification-service",
                "mnemos.account.events.v1"
        );
        allowConsumer(
                acls,
                "notification_service",
                "notification-service",
                "mnemos.event.events.v1"
        );
        allowConsumer(
                acls,
                "notification_service",
                "notification-service",
                "mnemos.moderation.events.v1"
        );
        allowConsumer(
                acls,
                "notification_service",
                "notification-service",
                "mnemos.personalization.events.v1"
        );

        allowTopicWrite(
                acls,
                "realtime_service",
                "mnemos.collector.ingress.v2"
        );
        allowConsumer(
                acls,
                "realtime_service",
                "realtime-service",
                "mnemos.wallet.events.v2"
        );
        allowConsumer(
                acls,
                "realtime_service",
                "realtime-service",
                "mnemos.bet.events.v1"
        );
        allowConsumer(
                acls,
                "realtime_service",
                "realtime-service",
                "mnemos.personalization.events.v1"
        );
        allowConsumer(
                acls,
                "realtime_service",
                "realtime-service",
                "mnemos.social.events.v1"
        );

        allowConsumer(
                acls,
                "personalization_service",
                "personalization-service",
                "mnemos.wallet.events.v2"
        );
        allowConsumer(
                acls,
                "personalization_service",
                "personalization-service",
                "mnemos.personalization.events.v1"
        );
        allowTopicWrite(
                acls,
                "personalization_service",
                "mnemos.wallet.commands.v2"
        );
        allowTopicWrite(
                acls,
                "personalization_service",
                "mnemos.personalization.events.v1"
        );

        allowConsumer(
                acls,
                "social_service",
                "social-service",
                "mnemos.wallet.events.v2"
        );
        allowTopicWrite(
                acls,
                "social_service",
                "mnemos.wallet.commands.v2"
        );
        allowTopicWrite(
                acls,
                "social_service",
                "mnemos.social.events.v1"
        );

        allowConsumer(
                acls,
                "profile_service",
                "profile-service",
                "mnemos.account.events.v1"
        );
        allowConsumer(
                acls,
                "profile_service",
                "profile-service",
                "mnemos.social.events.v1"
        );

        admin
                .createAcls(acls)
                .all()
                .get();

        System.out.println("Configured " + acls.size() + " Kafka ACLs.");
    }

    private static void allowConsumer(
            Set<AclBinding> acls,
            String principal,
            String serviceName,
            String topic
    ) {
        allowTopicRead(
                acls,
                principal,
                topic
        );

        allowTopicWrite(
                acls,
                principal,
                topic + ".dlt"
        );

        allowGroupPrefix(
                acls,
                principal,
                serviceName + "-"
        );
    }

    private static void allowTopicRead(
            Set<AclBinding> acls,
            String principal,
            String topic
    ) {
        addTopicAcl(
                acls,
                principal,
                topic,
                AclOperation.READ
        );

        addTopicAcl(
                acls,
                principal,
                topic,
                AclOperation.DESCRIBE
        );
    }

    private static void allowTopicWrite(
            Set<AclBinding> acls,
            String principal,
            String topic
    ) {
        addTopicAcl(
                acls,
                principal,
                topic,
                AclOperation.WRITE
        );

        addTopicAcl(
                acls,
                principal,
                topic,
                AclOperation.DESCRIBE
        );
    }

    private static void allowGroupPrefix(
            Set<AclBinding> acls,
            String principal,
            String prefix
    ) {
        ResourcePattern resourcePattern = new ResourcePattern(
                ResourceType.GROUP,
                prefix,
                PatternType.PREFIXED
        );

        AccessControlEntry accessControlEntry = new AccessControlEntry(
                "User:" + principal,
                "*",
                AclOperation.READ,
                AclPermissionType.ALLOW
        );

        acls.add(
                new AclBinding(
                        resourcePattern,
                        accessControlEntry
                )
        );
    }

    private static void addTopicAcl(
            Set<AclBinding> acls,
            String principal,
            String topic,
            AclOperation operation
    ) {
        ResourcePattern resourcePattern = new ResourcePattern(
                ResourceType.TOPIC,
                topic,
                PatternType.LITERAL
        );

        AccessControlEntry accessControlEntry = new AccessControlEntry(
                "User:" + principal,
                "*",
                operation,
                AclPermissionType.ALLOW
        );

        acls.add(
                new AclBinding(
                        resourcePattern,
                        accessControlEntry
                )
        );
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Environment variable " + name + " is required"
            );
        }

        return value;
    }
}
