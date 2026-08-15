package com.mnemos.personalization.domain.personalization

import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NotificationPackContentTest {
    @Test
    fun `notification pack rejects unsupported placeholders`() {
        val templates =
            NotificationTemplateKey.entries.associateWith { key ->
                NotificationTemplate(
                    emoji = null,
                    soloTitle = key.name,
                    soloBody = "Notification",
                    compact = "Notification",
                )
            }.toMutableMap()

        templates[NotificationTemplateKey.RAID] =
            NotificationTemplate(
                emoji = "⚔️",
                soloTitle = "Raid",
                soloBody = "Location {player}",
                compact = "Raid",
            )

        assertThrows(IllegalArgumentException::class.java) {
            PersonalizationContent.NotificationPack(templates)
        }
    }
}
