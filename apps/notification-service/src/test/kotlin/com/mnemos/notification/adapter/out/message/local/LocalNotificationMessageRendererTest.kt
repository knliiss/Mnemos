package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LocalNotificationMessageRendererTest {
    private val repository = FakeActiveNotificationPackRepository()
    private val renderer =
        LocalNotificationMessageRenderer(
            resolver = StandardMessageResolver(),
            personalizedResolver = PersonalizedMessageResolver(),
            activeNotificationPackRepository = repository,
        )

    @Test
    fun `solo item drop renders resolved message`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")
        val item = itemDrop(now, "Knalis", "Elucidator", NotificationItemRarity.SECRET)

        val message = renderer.renderNotification(UserId.generate(), listOf(item))

        assertEquals(
            "🗡️ <b>Новое секретное оружие</b>\n" +
                "Игрок <b>Knalis</b> выбил секретное оружие <b>«Elucidator»</b>.",
            message.text,
        )
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

            🗡️ <b>Knalis</b> — секретное оружие <b>«Elucidator»</b>

            ⚔️ <b>Asuna</b> — мифическое оружие <b>«Lambent Light»</b>
            """.trimIndent(),
            message.text,
        )
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
    ): NotificationItem =
        NotificationItem(
            sourceId = UUID.randomUUID(),
            payload =
                ItemDropNotificationPayload(
                    droppedFor = player,
                    itemName = item,
                    itemType = NotificationItemType.SWORD,
                    itemRarity = rarity,
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
                        NotificationPackTemplate(
                            emoji = null,
                            soloTitle = type.name,
                            soloBody = "Event",
                            compact = "Event",
                            aggregateTitle = "${type.name} events",
                        )
                    }
                },
        )

    private class FakeActiveNotificationPackRepository : ActiveNotificationPackRepository {
        var active: ActiveNotificationPack? = null

        override fun find(userId: UserId): ActiveNotificationPack? =
            active?.takeIf { it.userId == userId }

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
