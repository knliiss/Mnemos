import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class KafkaBootstrap {
    private static final short REPLICATION_FACTOR = 1;

    private static final Set<String> MANAGED_SERVICE_PRINCIPALS = Set.of(
            "User:account_service",
            "User:wallet_service",
            "User:event_service",
            "User:notification_service",
            "User:reward_service",
            "User:bet_service",
            "User:moderation_service",
            "User:gateway_service",
            "User:realtime_service",
            "User:personalization_service",
            "User:social_service",
            "User:profile_service",
            "User:player_directory_service",
            "User:payment_service"
    );

    private KafkaBootstrap() {
    }

    public static void main(String[] args) throws Exception {
        Properties properties = adminProperties();

        try (Admin admin = Admin.create(properties)) {
            createTopics(admin);
            reconcileAcls(admin);
        }

        System.out.println("Kafka topics and ACLs are configured.");
    }

    private static Properties adminProperties() {
        String bootstrapServer = requiredEnvironment("KAFKA_BOOTSTRAP_SERVER");
        String adminPassword = requiredEnvironment("KAFKA_ADMIN_PASSWORD");
        String tlsStorePassword = requiredEnvironment("KAFKA_TLS_STORE_PASSWORD");
        String tlsTruststoreLocation = requiredEnvironment("KAFKA_TLS_TRUSTSTORE_LOCATION");

        Properties properties = new Properties();

        properties.put("bootstrap.servers", bootstrapServer);
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.mechanism", "PLAIN");
        properties.put(
                "sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"kafka_admin\" "
                        + "password=\"" + adminPassword + "\";"
        );
        properties.put("ssl.truststore.location", tlsTruststoreLocation);
        properties.put("ssl.truststore.password", tlsStorePassword);
        properties.put("ssl.truststore.type", "PKCS12");

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

        topics.add(topic("mnemos.player-directory.events.v1", 3));
        topics.add(topic("mnemos.player-directory.events.v1.dlt", 3));

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

    private static void reconcileAcls(Admin admin) throws Exception {
        Set<AclBinding> desiredAcls = desiredAcls();
        Set<AclBinding> existingAcls = new LinkedHashSet<>(
                admin
                        .describeAcls(AclBindingFilter.ANY)
                        .values()
                        .get()
        );

        List<AclBinding> staleManagedAcls = existingAcls
                .stream()
                .filter(KafkaBootstrap::isManagedServiceAllowAcl)
                .filter(acl -> !desiredAcls.contains(acl))
                .toList();

        if (!staleManagedAcls.isEmpty()) {
            admin
                    .deleteAcls(
                            staleManagedAcls
                                    .stream()
                                    .map(AclBinding::toFilter)
                                    .toList()
                    )
                    .all()
                    .get();
        }

        List<AclBinding> missingAcls = desiredAcls
                .stream()
                .filter(acl -> !existingAcls.contains(acl))
                .toList();

        if (!missingAcls.isEmpty()) {
            admin
                    .createAcls(missingAcls)
                    .all()
                    .get();
        }

        System.out.println(
                "Kafka ACLs reconciled: "
                        + desiredAcls.size()
                        + " desired, "
                        + staleManagedAcls.size()
                        + " stale managed ACLs removed, "
                        + missingAcls.size()
                        + " missing ACLs created."
        );
    }

    private static boolean isManagedServiceAllowAcl(AclBinding acl) {
        AccessControlEntry entry = acl.entry();

        return entry.permissionType() == AclPermissionType.ALLOW
                && MANAGED_SERVICE_PRINCIPALS.contains(entry.principal());
    }

    private static Set<AclBinding> desiredAcls() {
        Set<AclBinding> acls = new LinkedHashSet<>();

        allowTopicWrite(
                acls,
                "account_service",
                "mnemos.account.events.v1"
        );
        allowConsumer(
                acls,
                "account_service",
                "account-service",
                "mnemos.player-directory.events.v1"
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
                "mnemos.moderation.events.v1"
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
        allowConsumer(
                acls,
                "event_service",
                "event-service",
                "mnemos.player-directory.events.v1"
        );
        allowConsumer(
                acls,
                "event_service",
                "event-service",
                "mnemos.moderation.events.v1"
        );
        allowTopicWrite(
                acls,
                "event_service",
                "mnemos.event.events.v1"
        );

        allowConsumer(
                acls,
                "player_directory_service",
                "player-directory-service",
                "mnemos.account.events.v1"
        );
        allowConsumer(
                acls,
                "player_directory_service",
                "player-directory-service",
                "mnemos.event.events.v1"
        );
        allowTopicWrite(
                acls,
                "player_directory_service",
                "mnemos.player-directory.events.v1"
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

        allowConsumer(
                acls,
                "moderation_service",
                "moderation-service",
                "mnemos.wallet.events.v2"
        );
        allowTopicWrite(
                acls,
                "moderation_service",
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
        allowConsumer(
                acls,
                "notification_service",
                "notification-service",
                "mnemos.player-directory.events.v1"
        );
        allowConsumer(
                acls,
                "notification_service",
                "notification-service",
                "mnemos.collector.ingress.v2"
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
                "mnemos.event.events.v1"
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
        allowConsumer(
                acls,
                "profile_service",
                "profile-service",
                "mnemos.personalization.events.v1"
        );
        allowConsumer(
                acls,
                "profile_service",
                "profile-service",
                "mnemos.player-directory.events.v1"
        );

        allowTopicWrite(
                acls,
                "payment_service",
                "mnemos.wallet.commands.v2"
        );
        allowConsumer(
                acls,
                "payment_service",
                "payment-service",
                "mnemos.wallet.events.v2"
        );

        return acls;
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
