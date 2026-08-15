package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.domain.personalization.model.EffectType
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

object PersonalizationContentPersistenceMapper {
    fun PersonalizationContent.toDocument(): Map<String, Any?> =
        when (this) {
            is PersonalizationContent.NickStyle ->
                mapOf(
                    "colors" to colors.map { it.toDocument() },
                    "angleDegrees" to angleDegrees,
                    "glow" to glow?.toDocument(),
                )

            is PersonalizationContent.ProfileTheme ->
                mapOf(
                    "background" to background.value,
                    "surface" to surface.value,
                    "accent" to accent.value,
                    "text" to text.value,
                    "border" to border.value,
                    "glow" to glow?.toDocument(),
                )

            is PersonalizationContent.AvatarFrame ->
                mapOf(
                    "borderColor" to borderColor.value,
                    "borderWidthPx" to borderWidthPx,
                    "radiusPx" to radiusPx,
                    "glow" to glow?.toDocument(),
                )

            is PersonalizationContent.ProfileEffect ->
                mapOf(
                    "effect" to effect.name,
                    "intensity" to intensity,
                    "primaryColor" to primaryColor.value,
                    "secondaryColor" to secondaryColor?.value,
                )

            is PersonalizationContent.NotificationPack ->
                mapOf(
                    "mixedAggregateTitle" to mixedAggregateTitle,
                    "templates" to
                        templates.mapKeys { (key, _) -> key.name }
                            .mapValues { (_, template) -> template.toDocument() },
                )
        }

    fun Map<String, Any?>.toDomain(type: PersonalizationType): PersonalizationContent =
        when (type) {
            PersonalizationType.NOTIFICATION_PACK -> toNotificationPack()
            PersonalizationType.NICK_STYLE -> toNickStyle()
            PersonalizationType.PROFILE_THEME -> toProfileTheme()
            PersonalizationType.AVATAR_FRAME -> toAvatarFrame()
            PersonalizationType.PROFILE_EFFECT -> toProfileEffect()
        }

    private fun Map<String, Any?>.toNickStyle(): PersonalizationContent.NickStyle =
        PersonalizationContent.NickStyle(
            colors =
                requireList("colors").map { value ->
                    requireDocument(value).toGradientStop()
                },
            angleDegrees = requireNumber("angleDegrees").toInt(),
            glow = documentOrNull("glow")?.toGlow(),
        )

    private fun Map<String, Any?>.toProfileTheme(): PersonalizationContent.ProfileTheme =
        PersonalizationContent.ProfileTheme(
            background = HexColor(requireString("background")),
            surface = HexColor(requireString("surface")),
            accent = HexColor(requireString("accent")),
            text = HexColor(requireString("text")),
            border = HexColor(requireString("border")),
            glow = documentOrNull("glow")?.toGlow(),
        )

    private fun Map<String, Any?>.toAvatarFrame(): PersonalizationContent.AvatarFrame =
        PersonalizationContent.AvatarFrame(
            borderColor = HexColor(requireString("borderColor")),
            borderWidthPx = requireNumber("borderWidthPx").toInt(),
            radiusPx = requireNumber("radiusPx").toInt(),
            glow = documentOrNull("glow")?.toGlow(),
        )

    private fun Map<String, Any?>.toProfileEffect(): PersonalizationContent.ProfileEffect =
        PersonalizationContent.ProfileEffect(
            effect = EffectType.valueOf(requireString("effect")),
            intensity = requireNumber("intensity").toInt(),
            primaryColor = HexColor(requireString("primaryColor")),
            secondaryColor = stringOrNull("secondaryColor")?.let(::HexColor),
        )

    private fun Map<String, Any?>.toNotificationPack(): PersonalizationContent.NotificationPack {
        val templates = requireDocument(requireValue("templates"))

        return PersonalizationContent.NotificationPack(
            mixedAggregateTitle = requireString("mixedAggregateTitle"),
            templates =
                templates.map { (rawKey, rawTemplate) ->
                    val key = NotificationTemplateKey.valueOf(rawKey)
                    val template = requireDocument(rawTemplate)

                    key to
                        NotificationTemplate(
                            emoji = template.stringOrNull("emoji"),
                            soloTitle = template.requireString("soloTitle"),
                            soloBody = template.requireString("soloBody"),
                            compact = template.requireString("compact"),
                            aggregateTitle = template.requireString("aggregateTitle"),
                        )
                }.toMap(),
        )
    }

    private fun GradientStop.toDocument(): Map<String, Any?> =
        mapOf(
            "color" to color.value,
            "position" to position,
        )

    private fun Map<String, Any?>.toGradientStop(): GradientStop =
        GradientStop(
            color = HexColor(requireString("color")),
            position = requireNumber("position").toInt(),
        )

    private fun GlowStyle.toDocument(): Map<String, Any?> =
        mapOf(
            "color" to color.value,
            "strength" to strength,
        )

    private fun Map<String, Any?>.toGlow(): GlowStyle =
        GlowStyle(
            color = HexColor(requireString("color")),
            strength = requireNumber("strength").toInt(),
        )

    private fun NotificationTemplate.toDocument(): Map<String, Any?> =
        mapOf(
            "emoji" to emoji,
            "soloTitle" to soloTitle,
            "soloBody" to soloBody,
            "compact" to compact,
            "aggregateTitle" to aggregateTitle,
        )

    private fun Map<String, Any?>.requireValue(key: String): Any =
        requireNotNull(this[key]) { "Missing personalization content field: $key" }

    private fun Map<String, Any?>.requireString(key: String): String =
        requireValue(key) as? String
            ?: throw IllegalArgumentException("Personalization content field $key must be a string")

    private fun Map<String, Any?>.stringOrNull(key: String): String? =
        when (val value = this[key]) {
            null -> null
            is String -> value
            else -> throw IllegalArgumentException("Personalization content field $key must be a string")
        }

    private fun Map<String, Any?>.requireNumber(key: String): Number =
        requireValue(key) as? Number
            ?: throw IllegalArgumentException("Personalization content field $key must be a number")

    private fun Map<String, Any?>.requireList(key: String): List<*> =
        requireValue(key) as? List<*>
            ?: throw IllegalArgumentException("Personalization content field $key must be a list")

    private fun Map<String, Any?>.documentOrNull(key: String): Map<String, Any?>? =
        this[key]?.let(::requireDocument)

    @Suppress("UNCHECKED_CAST")
    private fun requireDocument(value: Any?): Map<String, Any?> =
        value as? Map<String, Any?>
            ?: throw IllegalArgumentException("Personalization content value must be an object")
}
