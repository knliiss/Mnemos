package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.localization.model.NotificationLocalizationContext
import com.mnemos.notification.application.notification.localization.service.NotificationLocalizationResolver
import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LocalNotificationMessageRendererTest {
    private val repository = FakeActiveNotificationPackRepository()
    private var localization =
        NotificationLocalizationContext(
            language = LocalizationLanguage.RU,
            saoProperties = emptyMap(),
        )
    private val renderer =
        LocalNotificationMessageRenderer(
            resolver = StandardMessageResolver(),
            personalizedResolver = PersonalizedMessageResolver(),
            activeNotificationPackRepository = repository,
            localizationResolver = NotificationLocalizationResolver { localization },
        )

    @BeforeEach
    fun resetRendererState() {
        repository.active = null
        localization =
            NotificationLocalizationContext(
                language = LocalizationLanguage.RU,
                saoProperties = emptyMap(),
            )
    }

    @Test
    fun `solo item drop renders resolved message`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val item = itemDrop(now, "Knalis", "Elucidator", NotificationItemRarity.SECRET)

        val message = renderer.renderNotification(UserId.generate(), listOf(item))

        assertEquals(
            "⚔️ <b>Секретное оружие</b>\n" +
                "Игрок <b>Knalis</b> выбил секретное оружие <b>«Elucidator»</b>.",
            message.text,
        )
    }

    @Test
    fun `item key is rendered from the users SAO localization`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        localization =
            NotificationLocalizationContext(
                language = LocalizationLanguage.EN,
                saoProperties =
                    mapOf(
                        "sao.item.l1_1.name" to "¨efac00Elucidator",
                    ),
            )
        val item =
            itemDrop(
                now = now,
                player = "Knalis",
                item = "Элюцидатор",
                rarity = NotificationItemRarity.SECRET,
                itemKey = "sao.item.l1_1.name",
            )

        val message = renderer.renderNotification(UserId.generate(), listOf(item))

        assertTrue(message.text.contains("<b>“Elucidator”</b>"))
        assertTrue(message.text.contains("Player <b>Knalis</b> dropped"))
        assertFalse(message.text.contains("Элюцидатор"))
    }

    @Test
    fun `aggregated message renders all items`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val items =
            listOf(
                itemDrop(now, "Knalis", "Elucidator", NotificationItemRarity.SECRET),
                itemDrop(now, "Asuna", "Lambent Light", NotificationItemRarity.MYTHICAL),
            )

        val message = renderer.renderNotification(UserId.generate(), items)

        assertEquals(
            """
            🎁 <b>Новые находки</b>

            ⚔️ <b>Knalis</b> — секретное оружие <b>«Elucidator»</b>

            ✨ <b>Asuna</b> — мифический предмет <b>«Lambent Light»</b>
            """.trimIndent(),
            message.text,
        )
    }

    @Test
    fun `exact duplicate booster payload is rendered once`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val userId = UserId.generate()
        repository.active = activePack(userId)
        val items =
            listOf(
                booster(now, "Knalis", DomainBoosterType.MONEY),
                booster(now.plusMillis(200), "Knalis", DomainBoosterType.MONEY),
            )

        val message = renderer.renderNotification(userId, items)

        assertEquals("BOOSTER\nEvent", message.text)
    }

    @Test
    fun `active pack renders custom aggregate header and escapes values`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val userId = UserId.generate()
        repository.active = activePack(userId)
        val items =
            listOf(
                itemDrop(now, "<Knalis>", "Sword & Shield", NotificationItemRarity.SECRET),
                itemDrop(now, "Asuna", "Lambent Light", NotificationItemRarity.MYTHICAL),
            )

        val message = renderer.renderNotification(userId, items)

        assertTrue(message.text.startsWith("❄️ &lt;b&gt;Custom loot&lt;/b&gt;"))
        assertTrue(message.text.contains("&lt;Knalis&gt;"))
        assertTrue(message.text.contains("Sword &amp; Shield"))
    }

    @Test
    fun `custom pack text stays untouched while canonical item value is localized`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val userId = UserId.generate()
        repository.active = activePack(userId)
        localization =
            NotificationLocalizationContext(
                language = LocalizationLanguage.UK,
                saoProperties = mapOf("sao.item.l1_1.name" to "¨37efefЕлюцидатор"),
            )
        val item =
            itemDrop(
                now = now,
                player = "Knalis",
                item = "Legacy name",
                rarity = NotificationItemRarity.SECRET,
                itemKey = "sao.item.l1_1.name",
            )

        val message = renderer.renderNotification(userId, listOf(item))

        assertEquals("❄️ &lt;b&gt;Custom drop&lt;/b&gt;\nKnalis found Елюцидатор", message.text)
    }

    @Test
    fun `invalid active pack falls back to standard templates`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val userId = UserId.generate()
        repository.active = invalidPack(userId)
        val item = itemDrop(now, "Knalis", "Elucidator", NotificationItemRarity.SECRET)

        val message = renderer.renderNotification(userId, listOf(item))

        assertEquals(
            "⚔️ <b>Секретное оружие</b>\n" +
                "Игрок <b>Knalis</b> выбил секретное оружие <b>«Elucidator»</b>.",
            message.text,
        )
    }

    @Test
    fun `large aggregation stays within telegram message limit`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val items =
            List(200) { index ->
                NotificationItem(
                    sourceId = UUID.randomUUID(),
                    payload =
                        ItemDropNotificationPayload(
                            droppedFor = "Player$index",
                            itemName = "Item$index",
                            itemType = NotificationItemType.PET,
                            itemRarity = NotificationItemRarity.MYTHICAL,
                        ),
                    occurredAt = now,
                    expiresAt = now.plusSeconds(60),
                )
            }

        val message = renderer.renderNotification(UserId.generate(), items)

        assertTrue(message.text.length <= 4096)
        assertTrue(message.text.endsWith("…и ещё несколько событий"))
        assertEquals(
            message.text.count { it == '<' },
            message.text.count { it == '>' },
        )
    }

    @Test
    fun `html values are escaped`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val item = itemDrop(now, "<Knalis>", "Sword & Shield", NotificationItemRarity.SECRET)

        val message = renderer.renderNotification(UserId.generate(), listOf(item))

        assertTrue(message.text.contains("&lt;Knalis&gt;"))
        assertTrue(message.text.contains("Sword &amp; Shield"))
    }

    private fun itemDrop(
        now: Instant,
        player: String,
        item: String,
        rarity: NotificationItemRarity,
        itemKey: String? = null,
    ): NotificationItem =
        NotificationItem(
            sourceId = UUID.randomUUID(),
            payload =
                ItemDropNotificationPayload(
                    droppedFor = player,
                    itemKey = itemKey,
                    itemName = item,
                    itemType = NotificationItemType.SWORD,
                    itemRarity = rarity,
                ),
            occurredAt = now,
            expiresAt = now.plusSeconds(60),
        )

    private fun booster(
        now: Instant,
        player: String,
        type: DomainBoosterType,
    ): NotificationItem =
        NotificationItem(
            sourceId = UUID.randomUUID(),
            payload =
                BoosterNotificationPayload(
                    boosterType = type,
                    activatedBy = player,
                ),
            occurredAt = now,
            expiresAt = now.plusSeconds(60),
        )

    private fun activePack(userId: UserId): ActiveNotificationPack =
        ActiveNotificationPack(
            userId = userId,
            personalizationId = UUID.randomUUID(),
            mixedAggregateTitle = "🔔 <b>Custom events</b>",
            templates =
                NotificationType.entries.associateWith { type ->
                    if (type == NotificationType.ITEM_DROP) {
                        NotificationPackTemplate(
                            emoji = "❄️",
                            soloTitle = "<b>Custom drop</b>",
                            soloBody = "{player} found {item}",
                            compact = "{player} — {item}",
                            aggregateTitle = "<b>Custom loot</b>",
                        )
                    } else {
                        defaultTemplate(type)
                    }
                },
        )

    private fun invalidPack(userId: UserId): ActiveNotificationPack =
        ActiveNotificationPack(
            userId = userId,
            personalizationId = UUID.randomUUID(),
            mixedAggregateTitle = "Invalid events",
            templates =
                NotificationType.entries.associateWith { type ->
                    if (type == NotificationType.ITEM_DROP) {
                        NotificationPackTemplate(
                            emoji = null,
                            soloTitle = "{unknown}",
                            soloBody = "{player} found {item}",
                            compact = "{player} — {item}",
                            aggregateTitle = "Custom loot",
                        )
                    } else {
                        defaultTemplate(type)
                    }
                },
        )

    private fun defaultTemplate(type: NotificationType): NotificationPackTemplate =
        NotificationPackTemplate(
            emoji = null,
            soloTitle = type.name,
            soloBody = "Event",
            compact = "Event",
            aggregateTitle = "${type.name} events",
        )

    private class FakeActiveNotificationPackRepository : ActiveNotificationPackRepository {
        var active: ActiveNotificationPack? = null

        override fun find(userId: UserId): ActiveNotificationPack? = active?.takeIf { it.userId == userId }

        override fun apply(
            userId: UserId,
            pack: ActiveNotificationPack?,
            selectionRevision: Long,
            eventId: UUID,
            occurredAt: Instant,
        ) {
            active = pack?.takeIf { it.userId == userId }
        }
    }
}
