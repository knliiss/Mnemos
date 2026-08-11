package com.mnemos.notification.integration.subscription

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionCommand
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionUseCase
import com.mnemos.notification.application.common.pagination.PageQuery
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionCommand
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionUseCase
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionCommand
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionUseCase
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionCommand
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionUseCase
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionUseCase
import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionCommand
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionUseCase
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsQuery
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsUseCase
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.subscription.exception.SubscriptionNotFoundException
import com.mnemos.notification.domain.subscription.model.SubscriptionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "spring.kafka.listener.auto-startup=false",
        "mnemos.notification.processing.enabled=false",
    ],
)
class SubscriptionReadDeleteIntegrationTest {
    @Autowired
    private lateinit var itemDropUseCase: CreateItemDropSubscriptionUseCase

    @Autowired
    private lateinit var boosterUseCase: CreateBoosterSubscriptionUseCase

    @Autowired
    private lateinit var dungeonUseCase: CreateDungeonSubscriptionUseCase

    @Autowired
    private lateinit var raidUseCase: CreateRaidSubscriptionUseCase

    @Autowired
    private lateinit var globalUseCase: CreateGlobalSubscriptionUseCase

    @Autowired
    private lateinit var listUseCase: ListSubscriptionsUseCase

    @Autowired
    private lateinit var deleteUseCase: DeleteSubscriptionUseCase

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE subscriptions CASCADE")
    }

    @Test
    fun `reader returns all subscription types with subtype details`() {
        val userId = UserId.generate()

        itemDropUseCase.execute(
            CreateItemDropSubscriptionCommand(
                userId = userId,
                droppedFor = "Knalis",
                itemName = "Excalibur",
            ),
        )
        boosterUseCase.execute(CreateBoosterSubscriptionCommand(userId, DomainBoosterType.LUCK))
        dungeonUseCase.execute(CreateDungeonSubscriptionCommand(userId, DomainDungeonType.EXPERT))
        raidUseCase.execute(CreateRaidSubscriptionCommand(userId, setOf(5, 1, 8)))
        globalUseCase.execute(CreateGlobalSubscriptionCommand(userId, DomainGlobalEventType.ECLIPSE))

        val result =
            listUseCase.execute(
                ListSubscriptionsQuery(
                    userId = userId,
                    page = PageQuery(page = 0, size = 20),
                ),
            )

        assertEquals(5L, result.totalItems)
        assertEquals(5, result.items.size)

        val byType = result.items.associateBy { it.type }

        assertEquals(
            SubscriptionView.Details.ItemDrop("Knalis", "Excalibur"),
            byType.getValue(SubscriptionType.ITEM_DROP).details,
        )
        assertEquals(
            SubscriptionView.Details.Booster(DomainBoosterType.LUCK),
            byType.getValue(SubscriptionType.BOOSTER).details,
        )
        assertEquals(
            SubscriptionView.Details.Dungeon(DomainDungeonType.EXPERT),
            byType.getValue(SubscriptionType.DUNGEON).details,
        )
        assertEquals(
            SubscriptionView.Details.Raid(linkedSetOf(1, 5, 8)),
            byType.getValue(SubscriptionType.RAID).details,
        )
        assertEquals(
            SubscriptionView.Details.Global(DomainGlobalEventType.ECLIPSE),
            byType.getValue(SubscriptionType.GLOBAL).details,
        )
    }

    @Test
    fun `reader pages only current user subscriptions`() {
        val userId = UserId.generate()
        val otherUserId = UserId.generate()

        boosterUseCase.execute(CreateBoosterSubscriptionCommand(userId, DomainBoosterType.LUCK))
        boosterUseCase.execute(CreateBoosterSubscriptionCommand(userId, DomainBoosterType.MONEY))
        dungeonUseCase.execute(CreateDungeonSubscriptionCommand(userId, DomainDungeonType.HARD))
        globalUseCase.execute(CreateGlobalSubscriptionCommand(otherUserId, DomainGlobalEventType.MOON))

        val firstPage =
            listUseCase.execute(
                ListSubscriptionsQuery(
                    userId = userId,
                    page = PageQuery(page = 0, size = 2),
                ),
            )

        val secondPage =
            listUseCase.execute(
                ListSubscriptionsQuery(
                    userId = userId,
                    page = PageQuery(page = 1, size = 2),
                ),
            )

        assertEquals(3L, firstPage.totalItems)
        assertEquals(2, firstPage.items.size)
        assertEquals(2, firstPage.totalPages)
        assertEquals(true, firstPage.hasNextPage)
        assertEquals(1, secondPage.items.size)
        assertEquals(false, secondPage.hasNextPage)
    }

    @Test
    fun `delete removes owned subscription and cascades subtype`() {
        val userId = UserId.generate()
        val created = boosterUseCase.execute(CreateBoosterSubscriptionCommand(userId, DomainBoosterType.POWER))

        deleteUseCase.execute(
            DeleteSubscriptionCommand(
                subscriptionId = created.subscriptionId,
                userId = userId,
            ),
        )

        assertEquals(0L, count("subscriptions"))
        assertEquals(0L, count("booster_subscriptions"))
    }

    @Test
    fun `delete does not delete subscription owned by another user`() {
        val ownerId = UserId.generate()
        val attackerId = UserId.generate()
        val created = dungeonUseCase.execute(CreateDungeonSubscriptionCommand(ownerId, DomainDungeonType.EASY))

        assertThrows(SubscriptionNotFoundException::class.java) {
            deleteUseCase.execute(
                DeleteSubscriptionCommand(
                    subscriptionId = created.subscriptionId,
                    userId = attackerId,
                ),
            )
        }

        assertEquals(1L, count("subscriptions"))
        assertEquals(1L, count("dungeon_subscriptions"))
    }

    private fun count(table: String): Long =
        requireNotNull(
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM $table",
                Long::class.java,
            ),
        )

    companion object {
        private val postgres =
            PostgreSQLContainer("postgres:17-alpine")
                .withDatabaseName("mnemos")
                .withUsername("mnemos")
                .withPassword("mnemos")

        init {
            postgres.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configurePostgres(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                val jdbcUrl = postgres.jdbcUrl
                val separator = if (jdbcUrl.contains("?")) "&" else "?"
                "$jdbcUrl${separator}currentSchema=notification"
            }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }
}
