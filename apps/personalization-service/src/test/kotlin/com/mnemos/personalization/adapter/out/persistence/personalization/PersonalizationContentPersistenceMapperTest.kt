package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.adapter.out.persistence.personalization.content.PersonalizationContentPersistenceMapper.toDocument
import com.mnemos.personalization.adapter.out.persistence.personalization.content.PersonalizationContentPersistenceMapper.toDomain
import com.mnemos.personalization.domain.personalization.model.EffectType
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
    fun `profile theme survives document round trip`() {
        val content =
            PersonalizationContent.ProfileTheme(
                background = HexColor("#05070A"),
                surface = HexColor("#10151C"),
                accent = HexColor("#A8DDFF"),
                text = HexColor("#F5FBFF"),
                border = HexColor("#334455"),
                glow =
                    GlowStyle(
                        color = HexColor("#BCE8FF"),
                        strength = 35,
                    ),
            )

        val restored = content.toDocument().toDomain(PersonalizationType.PROFILE_THEME)

        assertEquals(content, restored)
    }

    @Test
    fun `avatar frame survives document round trip`() {
        val content =
            PersonalizationContent.AvatarFrame(
                borderColor = HexColor("#DDEEFF"),
                borderWidthPx = 4,
                radiusPx = 18,
                glow =
                    GlowStyle(
                        color = HexColor("#FFFFFF"),
                        strength = 60,
                    ),
            )

        val restored = content.toDocument().toDomain(PersonalizationType.AVATAR_FRAME)

        assertEquals(content, restored)
    }

    @Test
    fun `profile effect survives document round trip`() {
        val content =
            PersonalizationContent.ProfileEffect(
                effect = EffectType.AURORA,
                intensity = 70,
                primaryColor = HexColor("#8ACBFF"),
                secondaryColor = HexColor("#D7F3FF"),
            )

        val restored = content.toDocument().toDomain(PersonalizationType.PROFILE_EFFECT)

        assertEquals(content, restored)
    }

    @Test
    fun `notification pack survives document round trip`() {
        val content =
            PersonalizationContent.NotificationPack(
                mixedAggregateTitle = "🔔 Несколько событий",
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

    @Test
    fun `numeric content restores from long json numbers`() {
        val document =
            mapOf<String, Any?>(
                "borderColor" to "#DDEEFF",
                "borderWidthPx" to 4L,
                "radiusPx" to 18L,
                "glow" to
                    mapOf<String, Any?>(
                        "color" to "#FFFFFF",
                        "strength" to 60L,
                    ),
            )

        val restored = document.toDomain(PersonalizationType.AVATAR_FRAME)

        assertEquals(
            PersonalizationContent.AvatarFrame(
                borderColor = HexColor("#DDEEFF"),
                borderWidthPx = 4,
                radiusPx = 18,
                glow =
                    GlowStyle(
                        color = HexColor("#FFFFFF"),
                        strength = 60,
                    ),
            ),
            restored,
        )
    }
}
