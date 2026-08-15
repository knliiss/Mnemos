package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class PersonalizedMessageResolverSecurityTest {
    private val resolver = PersonalizedMessageResolver()

    @Test
    fun `creator controlled markup is rendered as text and placeholder values are escaped`() {
        val userId = UserId.generate()
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
                occurredAt = Instant.parse("2026-08-15T16:00:00Z"),
                expiresAt = Instant.parse("2026-08-15T16:01:00Z"),
            )
        val pack = activePack(userId)

        val resolved = resolver.resolve(item, pack)

        assertEquals("&lt;i&gt;❄️& $resolved", resolved.soloTitle)
    }

    private fun activePack(userId: UserId): ActiveNotificationPack =
        ActiveNotificationPack(
            userId = userId,
            personalizationId = UUID.randomUUID(),
            mixedAggregateTitle = "Events",
            templates =
                NotificationType.entries.associateWith { type ->
                    if (type == NotificationType.ITEM_DROP) {
                        NotificationPackTemplate(
                            emoji = "<i>❄️&",
                            soloTitle = "<b>{player}</b> & {item}",
                            soloBody = "{player} found {item}",
                            compact = "{player} — {item}",
                            aggregateTitle = "<b>Loot</b>",
                        )
                    } else {
                        NotificationPackTemplate(
                            emoji = null,
                            soloTitle = type.name,
                            soloBody = "Event",
                            compact = "Event",
                            aggregateTitle = "Events",
                        )
                    }
                },
        )
}
