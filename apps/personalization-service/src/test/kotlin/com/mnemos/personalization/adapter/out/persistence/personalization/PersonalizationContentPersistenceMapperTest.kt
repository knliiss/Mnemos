package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationContentPersistenceMapper.toDocument
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationContentPersistenceMapper.toDomain
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PersonalizationContentPersistenceMapperTest {
    @Test
    fun `nick style survives document round trip`() {
        val content =
            PersonalizationContent.NickStyle(
                colors =
                    listOf(
                        GradientStop(
                            color = HexColor("#F5FBFF"),
                            position = 0,
                        ),
                        GradientStop(
                            color = HexColor("#8ACBFF"),
                            position = 100,
                        ),
                    ),
                angleDegrees = 90,
                glow =
                    GlowStyle(
                        color = HexColor("#FFFFFF"),
                        strength = 45,
                    ),
            )

        val restored = content.toDocument().toDomain(PersonalizationType.NICK_STYLE)

        assertEquals(content, restored)
    }

    @Test
    fun `notification pack survives document round trip`() {
        val content =
            PersonalizationContent.NotificationPack(
                mixedAggregateTitle = "🔔 <b>Несколько событий</b>",
                templates =
                    NotificationTemplateKey.entries.associateWith { key ->
                        NotificationTemplate(
                            emoji = null,
                            soloTitle = "Событие",
                            soloBody = "Описание",
                            compact = "Коротко",
                            aggregateTitle = "${key.name} события",
                        )
                    },
            )

        val restored = content.toDocument().toDomain(PersonalizationType.NOTIFICATION_PACK)

        assertEquals(content, restored)
    }
}
