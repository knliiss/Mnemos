package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LocalNotificationMessageRendererTest {
    private val renderer =
        LocalNotificationMessageRenderer(
            resolver = StandardMessageResolver(),
        )

    @Test
    fun `solo item drop renders resolved message`() {
        val now = Instant.parse("2026-08-11T16:00:00Z")

        val item =
            NotificationItem(
                sourceId = UUID.randomUUID(),
                payload =
                    ItemDropNotificationPayload(
                        droppedFor = "Knalis",
                        itemName = "Elucidator",
                        itemType = NotificationItemType.SWORD,
                        itemRarity = NotificationItemRarity.SECRET,
                    ),
                occurredAt = now,
                expiresAt = now.plusSeconds(60),
            )

        val message =
            renderer.renderNotification(
                UserId(UUID.randomUUID()),
                listOf(item),
            )

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
                NotificationItem(
                    sourceId = UUID.randomUUID(),
                    payload =
                        ItemDropNotificationPayload(
                            droppedFor = "Knalis",
                            itemName = "Elucidator",
                            itemType = NotificationItemType.SWORD,
                            itemRarity = NotificationItemRarity.SECRET,
                        ),
                    occurredAt = now,
                    expiresAt = now.plusSeconds(60),
                ),
                NotificationItem(
                    sourceId = UUID.randomUUID(),
                    payload =
                        ItemDropNotificationPayload(
                            droppedFor = "Asuna",
                            itemName = "Lambent Light",
                            itemType = NotificationItemType.SWORD,
                            itemRarity = NotificationItemRarity.MYTHICAL,
                        ),
                    occurredAt = now,
                    expiresAt = now.plusSeconds(60),
                ),
            )

        val message =
            renderer.renderNotification(
                UserId(UUID.randomUUID()),
                items,
            )

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

        val message =
            renderer.renderNotification(
                UserId(UUID.randomUUID()),
                items,
            )

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

        val item =
            NotificationItem(
                sourceId = UUID.randomUUID(),
                payload =
                    ItemDropNotificationPayload(
                        droppedFor = "<Knalis>",
                        itemName = "Sword & Shield",
                        itemType = NotificationItemType.SWORD,
                        itemRarity = NotificationItemRarity.SECRET,
                    ),
                occurredAt = now,
                expiresAt = now.plusSeconds(60),
            )

        val message =
            renderer.renderNotification(
                UserId(UUID.randomUUID()),
                listOf(item),
            )

        assertTrue(message.text.contains("&lt;Knalis&gt;"))
        assertTrue(message.text.contains("Sword &amp; Shield"))
    }
}
