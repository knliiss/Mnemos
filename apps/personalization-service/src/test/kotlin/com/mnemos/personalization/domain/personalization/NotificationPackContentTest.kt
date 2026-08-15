package com.mnemos.personalization.domain.personalization

import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NotificationPackContentTest {
    @Test
    fun `notification pack rejects unsupported placeholders`() {
        val templates = validTemplates().toMutableMap()

        templates[NotificationTemplateKey.RAID] =
            NotificationTemplate(
                emoji = "⚔️",
                soloTitle = "Raid",
                soloBody = "Location {player}",
                compact = "Raid",
                aggregateTitle = "Raids",
            )

        assertThrows(IllegalArgumentException::class.java) {
            PersonalizationContent.NotificationPack(
                mixedAggregateTitle = "Several events",
                templates = templates,
            )
        }
    }

    @Test
    fun `notification pack rejects event placeholders in aggregate title`() {
        val templates = validTemplates().toMutableMap()

        templates[NotificationTemplateKey.RAID] =
            templates.getValue(NotificationTemplateKey.RAID).copy(
                aggregateTitle = "Raids at {locations}",
            )

        assertThrows(IllegalArgumentException::class.java) {
            PersonalizationContent.NotificationPack(
                mixedAggregateTitle = "Several events",
                templates = templates,
            )
        }
    }

    private fun validTemplates(): Map<NotificationTemplateKey, NotificationTemplate> =
        NotificationTemplateKey.entries.associateWith { key ->
            NotificationTemplate(
                emoji = null,
                soloTitle = key.name,
                soloBody = "Notification",
                compact = "Notification",
                aggregateTitle = "${key.name} events",
            )
        }
}
