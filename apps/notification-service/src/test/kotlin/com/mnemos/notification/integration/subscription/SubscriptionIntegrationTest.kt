package com.mnemos.notification.integration.subscription

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionCommand
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionUseCase
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionCommand
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionUseCase
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionCommand
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionUseCase
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionCommand
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionUseCase
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionUseCase
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "spring.kafka.listener.auto-startup=false",
        "mnemos.notification.processing.enabled=false",
    ],
)
class SubscriptionIntegrationTest {
    @Autowired
    private lateinit var itemDropSubscriptionUseCase: CreateItemDropSubscriptionUseCase

    @Autowired
    private lateinit var boosterSubscriptionUseCase: CreateBoosterSubscriptionUseCase

    @Autowired
    private lateinit var dungeonSubscriptionUseCase: CreateDungeonSubscriptionUseCase

    @Autowired
    private lateinit var globalSubscriptionUseCase: CreateGlobalSubscriptionUseCase

    @Autowired
    private lateinit var raidSubscriptionUseCase: CreateRaidSubscriptionUseCase

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE subscriptions CASCADE")
    }

    @Test
    fun `item drop equivalent subscriptions return same id`() {
        val userId = userId()

        val first =
            itemDropSubscriptionUseCase.execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = "Knalis",
                    itemName = "Excalibur",
                ),
            )

        val equivalent =
            itemDropSubscriptionUseCase.execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = "knalis",
                    itemName = "EXCALIBUR",
                ),
            )

        assertEquals(first.subscriptionId, equivalent.subscriptionId)
        assertEquals(1L, subscriptionCount(userId, "ITEM_DROP"))
        assertEquals(1L, tableCount("item_drop_subscriptions"))
    }

    @Test
    fun `item drop different filters create different subscriptions`() {
        val userId = userId()

        val both =
            itemDropSubscriptionUseCase.execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = "Knalis",
                    itemName = "Excalibur",
                ),
            )

        val playerOnly =
            itemDropSubscriptionUseCase.execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = "Knalis",
                    itemName = null,
                ),
            )

        val itemOnly =
            itemDropSubscriptionUseCase.execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = null,
                    itemName = "Excalibur",
                ),
            )

        val anotherItem =
            itemDropSubscriptionUseCase.execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = "Knalis",
                    itemName = "Aura",
                ),
            )

        assertNotEquals(both.subscriptionId, playerOnly.subscriptionId)
        assertNotEquals(both.subscriptionId, itemOnly.subscriptionId)
        assertNotEquals(both.subscriptionId, anotherItem.subscriptionId)
        assertNotEquals(playerOnly.subscriptionId, itemOnly.subscriptionId)

        assertEquals(4L, subscriptionCount(userId, "ITEM_DROP"))
        assertEquals(4L, tableCount("item_drop_subscriptions"))
    }

    @Test
    fun `booster equivalence includes user and booster type`() {
        val firstUser = userId()
        val secondUser = userId()

        val first =
            boosterSubscriptionUseCase.execute(
                CreateBoosterSubscriptionCommand(
                    userId = firstUser,
                    boosterType = DomainBoosterType.LUCK,
                ),
            )

        val equivalent =
            boosterSubscriptionUseCase.execute(
                CreateBoosterSubscriptionCommand(
                    userId = firstUser,
                    boosterType = DomainBoosterType.LUCK,
                ),
            )

        val anotherType =
            boosterSubscriptionUseCase.execute(
                CreateBoosterSubscriptionCommand(
                    userId = firstUser,
                    boosterType = DomainBoosterType.MONEY,
                ),
            )

        val anotherUser =
            boosterSubscriptionUseCase.execute(
                CreateBoosterSubscriptionCommand(
                    userId = secondUser,
                    boosterType = DomainBoosterType.LUCK,
                ),
            )

        assertEquals(first.subscriptionId, equivalent.subscriptionId)
        assertNotEquals(first.subscriptionId, anotherType.subscriptionId)
        assertNotEquals(first.subscriptionId, anotherUser.subscriptionId)

        assertEquals(2L, subscriptionCount(firstUser, "BOOSTER"))
        assertEquals(1L, subscriptionCount(secondUser, "BOOSTER"))
        assertEquals(3L, tableCount("booster_subscriptions"))
    }

    @Test
    fun `dungeon equivalence includes user and dungeon type`() {
        val firstUser = userId()
        val secondUser = userId()

        val first =
            dungeonSubscriptionUseCase.execute(
                CreateDungeonSubscriptionCommand(
                    userId = firstUser,
                    dungeonType = DomainDungeonType.EXPERT,
                ),
            )

        val equivalent =
            dungeonSubscriptionUseCase.execute(
                CreateDungeonSubscriptionCommand(
                    userId = firstUser,
                    dungeonType = DomainDungeonType.EXPERT,
                ),
            )

        val anotherType =
            dungeonSubscriptionUseCase.execute(
                CreateDungeonSubscriptionCommand(
                    userId = firstUser,
                    dungeonType = DomainDungeonType.HARD,
                ),
            )

        val anotherUser =
            dungeonSubscriptionUseCase.execute(
                CreateDungeonSubscriptionCommand(
                    userId = secondUser,
                    dungeonType = DomainDungeonType.EXPERT,
                ),
            )

        assertEquals(first.subscriptionId, equivalent.subscriptionId)
        assertNotEquals(first.subscriptionId, anotherType.subscriptionId)
        assertNotEquals(first.subscriptionId, anotherUser.subscriptionId)

        assertEquals(2L, subscriptionCount(firstUser, "DUNGEON"))
        assertEquals(1L, subscriptionCount(secondUser, "DUNGEON"))
        assertEquals(3L, tableCount("dungeon_subscriptions"))
    }

    @Test
    fun `global equivalence includes user and event type`() {
        val firstUser = userId()
        val secondUser = userId()

        val first =
            globalSubscriptionUseCase.execute(
                CreateGlobalSubscriptionCommand(
                    userId = firstUser,
                    eventType = DomainGlobalEventType.DARKNESS,
                ),
            )

        val equivalent =
            globalSubscriptionUseCase.execute(
                CreateGlobalSubscriptionCommand(
                    userId = firstUser,
                    eventType = DomainGlobalEventType.DARKNESS,
                ),
            )

        val anotherType =
            globalSubscriptionUseCase.execute(
                CreateGlobalSubscriptionCommand(
                    userId = firstUser,
                    eventType = DomainGlobalEventType.MOON,
                ),
            )

        val anotherUser =
            globalSubscriptionUseCase.execute(
                CreateGlobalSubscriptionCommand(
                    userId = secondUser,
                    eventType = DomainGlobalEventType.DARKNESS,
                ),
            )

        assertEquals(first.subscriptionId, equivalent.subscriptionId)
        assertNotEquals(first.subscriptionId, anotherType.subscriptionId)
        assertNotEquals(first.subscriptionId, anotherUser.subscriptionId)

        assertEquals(2L, subscriptionCount(firstUser, "GLOBAL"))
        assertEquals(1L, subscriptionCount(secondUser, "GLOBAL"))
        assertEquals(3L, tableCount("global_subscriptions"))
    }

    @Test
    fun `raid locations are compared as a set`() {
        val userId = userId()

        val first =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = setOf(1, 3, 5),
                ),
            )

        val reordered =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = setOf(5, 1, 3),
                ),
            )

        val subset =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = setOf(1, 3),
                ),
            )

        assertEquals(first.subscriptionId, reordered.subscriptionId)
        assertNotEquals(first.subscriptionId, subset.subscriptionId)

        assertEquals(2L, subscriptionCount(userId, "RAID"))
        assertEquals(2L, tableCount("raid_subscriptions"))
        assertEquals(5L, tableCount("raid_subscription_locations"))
    }

    @Test
    fun `empty raid locations mean all locations and are equivalent only to empty set`() {
        val userId = userId()

        val allLocations =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = emptySet(),
                ),
            )

        val equivalent =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = emptySet(),
                ),
            )

        val specificLocation =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = setOf(1),
                ),
            )

        assertEquals(allLocations.subscriptionId, equivalent.subscriptionId)
        assertNotEquals(allLocations.subscriptionId, specificLocation.subscriptionId)

        assertEquals(2L, subscriptionCount(userId, "RAID"))
        assertEquals(2L, tableCount("raid_subscriptions"))
        assertEquals(1L, tableCount("raid_subscription_locations"))
    }

    @Test
    fun `same raid filters for different users create different subscriptions`() {
        val firstUser = userId()
        val secondUser = userId()

        val first =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = firstUser,
                    locations = setOf(1, 3),
                ),
            )

        val second =
            raidSubscriptionUseCase.execute(
                CreateRaidSubscriptionCommand(
                    userId = secondUser,
                    locations = setOf(1, 3),
                ),
            )

        assertNotEquals(first.subscriptionId, second.subscriptionId)

        assertEquals(1L, subscriptionCount(firstUser, "RAID"))
        assertEquals(1L, subscriptionCount(secondUser, "RAID"))
        assertEquals(2L, tableCount("raid_subscriptions"))
    }

    @Test
    fun `concurrent equivalent creates produce one subscription and same id`() {
        val userId = userId()

        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        try {
            val tasks =
                List(2) {
                    executor.submit<UUID> {
                        ready.countDown()
                        start.await()

                        boosterSubscriptionUseCase
                            .execute(
                                CreateBoosterSubscriptionCommand(
                                    userId = userId,
                                    boosterType = DomainBoosterType.LUCK,
                                ),
                            ).subscriptionId.id
                    }
                }

            check(ready.await(5, TimeUnit.SECONDS)) {
                "Create tasks did not become ready"
            }

            start.countDown()

            val firstId = tasks[0].get(10, TimeUnit.SECONDS)
            val secondId = tasks[1].get(10, TimeUnit.SECONDS)

            assertEquals(firstId, secondId)
            assertEquals(1L, subscriptionCount(userId, "BOOSTER"))
            assertEquals(1L, tableCount("booster_subscriptions"))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `database rejects subtype when subscription type does not match`() {
        assertWrongSubtypeRejected("BOOSTER") { id ->
            jdbcTemplate.update(
                """
                INSERT INTO item_drop_subscriptions (
                    subscription_id,
                    dropped_for
                )
                VALUES (?, ?)
                """.trimIndent(),
                id,
                "Knalis",
            )
        }

        assertWrongSubtypeRejected("RAID") { id ->
            jdbcTemplate.update(
                """
                INSERT INTO booster_subscriptions (
                    subscription_id,
                    booster_type
                )
                VALUES (?, ?)
                """.trimIndent(),
                id,
                "LUCK",
            )
        }

        assertWrongSubtypeRejected("GLOBAL") { id ->
            jdbcTemplate.update(
                """
                INSERT INTO dungeon_subscriptions (
                    subscription_id,
                    dungeon_type
                )
                VALUES (?, ?)
                """.trimIndent(),
                id,
                "EASY",
            )
        }

        assertWrongSubtypeRejected("DUNGEON") { id ->
            jdbcTemplate.update(
                """
                INSERT INTO global_subscriptions (
                    subscription_id,
                    event_type
                )
                VALUES (?, ?)
                """.trimIndent(),
                id,
                "DARKNESS",
            )
        }

        assertWrongSubtypeRejected("ITEM_DROP") { id ->
            jdbcTemplate.update(
                """
                INSERT INTO raid_subscriptions (
                    subscription_id
                )
                VALUES (?)
                """.trimIndent(),
                id,
            )
        }
    }

    private fun assertWrongSubtypeRejected(
        parentType: String,
        insertSubtype: (UUID) -> Unit,
    ) {
        val transactionTemplate = TransactionTemplate(transactionManager)
        val subscriptionId = UUID.randomUUID()

        assertThrows(DataIntegrityViolationException::class.java) {
            transactionTemplate.executeWithoutResult {
                jdbcTemplate.update(
                    """
                    INSERT INTO subscriptions (
                        id,
                        user_id,
                        type,
                        equivalence_key,
                        created_at
                    )
                    VALUES (?, ?, ?, ?, now())
                    """.trimIndent(),
                    subscriptionId,
                    UUID.randomUUID(),
                    parentType,
                    UUID.randomUUID().toString(),
                )

                insertSubtype(subscriptionId)

                // FK у нас DEFERRABLE INITIALLY DEFERRED.
                // Примусово перевіряємо його зараз, а не в момент commit.
                jdbcTemplate.execute("SET CONSTRAINTS ALL IMMEDIATE")
            }
        }
    }

    private fun subscriptionCount(
        userId: UserId,
        type: String,
    ): Long =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM subscriptions
            WHERE user_id = ?
              AND type = ?
            """.trimIndent(),
            Long::class.javaObjectType,
            userId.id,
            type,
        ) ?: 0L

    private fun tableCount(table: String): Long =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM $table",
            Long::class.javaObjectType,
        ) ?: 0L

    private fun userId(): UserId = UserId(UUID.randomUUID())

    companion object {
        private val postgres =
            PostgreSQLContainer("postgres:17-alpine")
                .withDatabaseName("mnemos")
                .withUsername("mnemos")
                .withPassword("mnemos")
                .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun configurePostgres(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                val jdbcUrl = postgres.jdbcUrl
                val separator = if (jdbcUrl.contains("?")) "&" else "?"
                "$jdbcUrl${separator}currentSchema=notification"
            }

            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
