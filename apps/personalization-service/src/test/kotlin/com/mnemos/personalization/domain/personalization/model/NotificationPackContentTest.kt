package com.mnemos.personalization.domain.personalization.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NotificationPackContentTest {
    @Test
    fun `raw telegram html is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            notificationPack(
                itemDrop =
                    template(
                        soloTitle = "<b>{player}</b>",
                    ),
            )
        }
    }

    @Test
    fun `malformed placeholder is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            notificationPack(
                itemDrop =
                    template(
                        soloBody = "{player found {item}",
                    ),
            )
        }
    }

    @Test
    fun `unsupported placeholder is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            notificationPack(
                itemDrop =
                    template(
                        compact = "{username} found {item}",
                    ),
            )
        }
    }

    @Test
    fun `aggregate title cannot contain placeholder`() {
        assertThrows(IllegalArgumentException::class.java) {
            notificationPack(
                itemDrop =
                    template(
                        aggregateTitle = "Drops for {player}",
                    ),
            )
        }
    }

    private fun notificationPack(itemDrop: NotificationTemplate): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = "Several events",
            templates =
                NotificationTemplateKey.entries.associateWith { key ->
                    if (key == NotificationTemplateKey.ITEM_DROP) {
                        itemDrop
                    } else {
                        template()
                    }
                },
        )

    private fun template(
        soloTitle: String = "Event title",
        soloBody: String = "Event body",
        compact: String = "Event compact",
        aggregateTitle: String = "Events",
    ): NotificationTemplate =
        NotificationTemplate(
            emoji = "❄️",
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )
}
