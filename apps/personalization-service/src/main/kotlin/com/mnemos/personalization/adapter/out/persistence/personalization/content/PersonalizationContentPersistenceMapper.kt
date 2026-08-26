package com.mnemos.personalization.adapter.out.persistence.personalization.content

import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.personalization.model.EffectType
import com.mnemos.personalization.domain.personalization.model.FrameStyle
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateCondition
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.ProfileBackgroundContent
import com.mnemos.personalization.domain.personalization.model.VisualLayer
import com.mnemos.personalization.domain.personalization.model.VisualLayerAnimation
import com.mnemos.personalization.domain.personalization.model.VisualLayerBlendMode
import java.util.UUID

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
                    "backgroundSecondary" to backgroundSecondary.value,
                    "surface" to surface.value,
                    "surfaceSecondary" to surfaceSecondary.value,
                    "accent" to accent.value,
                    "text" to text.value,
                    "border" to border.value,
                    "gradientAngleDegrees" to gradientAngleDegrees,
                    "textureStrength" to textureStrength,
                    "glow" to glow?.toDocument(),
                    "layers" to layers.map { layer -> layer.toDocument() },
                )

            is ProfileBackgroundContent ->
                mapOf(
                    "effect" to effect.name,
                    "intensity" to intensity,
                    "primaryColor" to primaryColor.value,
                    "secondaryColor" to secondaryColor?.value,
                    "speed" to speed,
                    "density" to density,
                    "scale" to scale,
                    "layers" to layers.map { layer -> layer.toDocument() },
                )

            is PersonalizationContent.AvatarFrame ->
                mapOf(
                    "borderColor" to borderColor.value,
                    "secondaryBorderColor" to secondaryBorderColor.value,
                    "borderWidthPx" to borderWidthPx,
                    "gapPx" to gapPx,
                    "radiusPx" to radiusPx,
                    "style" to style.name,
                    "glow" to glow?.toDocument(),
                    "layers" to layers.map { layer -> layer.toDocument() },
                )

            is PersonalizationContent.ProfileEffect ->
                mapOf(
                    "effect" to effect.name,
                    "intensity" to intensity,
                    "primaryColor" to primaryColor.value,
                    "secondaryColor" to secondaryColor?.value,
                    "speed" to speed,
                    "density" to density,
                    "scale" to scale,
                    "layers" to layers.map { layer -> layer.toDocument() },
                )

            is PersonalizationContent.NotificationPack ->
                mapOf(
                    "mixedAggregateTitle" to mixedAggregateTitle,
                    "templates" to
                        templates
                            .mapKeys { (key, _) -> key.name }
                            .mapValues { (_, template) -> template.toDocument() },
                    "valueMappings" to valueMappings.toDocument(),
                )
        }

    fun Map<String, Any?>.toDomain(type: PersonalizationType): PersonalizationContent =
        when (type) {
            PersonalizationType.NOTIFICATION_PACK -> toNotificationPack()
            PersonalizationType.NICK_STYLE -> toNickStyle()
            PersonalizationType.PROFILE_THEME -> toProfileTheme()
            PersonalizationType.PROFILE_BACKGROUND -> toProfileBackground()
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

    private fun Map<String, Any?>.toProfileTheme(): PersonalizationContent.ProfileTheme {
        val background = HexColor(requireString("background"))
        val surface = HexColor(requireString("surface"))

        return PersonalizationContent.ProfileTheme(
            background = background,
            backgroundSecondary = stringOrNull("backgroundSecondary")?.let(::HexColor) ?: background,
            surface = surface,
            surfaceSecondary = stringOrNull("surfaceSecondary")?.let(::HexColor) ?: surface,
            accent = HexColor(requireString("accent")),
            text = HexColor(requireString("text")),
            border = HexColor(requireString("border")),
            gradientAngleDegrees = numberOrNull("gradientAngleDegrees")?.toInt() ?: 145,
            textureStrength = numberOrNull("textureStrength")?.toInt() ?: 0,
            glow = documentOrNull("glow")?.toGlow(),
            layers = visualLayers(),
        )
    }

    private fun Map<String, Any?>.toProfileBackground(): ProfileBackgroundContent =
        ProfileBackgroundContent(
            effect = EffectType.valueOf(requireString("effect")),
            intensity = requireNumber("intensity").toInt(),
            primaryColor = HexColor(requireString("primaryColor")),
            secondaryColor = stringOrNull("secondaryColor")?.let(::HexColor),
            speed = numberOrNull("speed")?.toInt() ?: 50,
            density = numberOrNull("density")?.toInt() ?: 50,
            scale = numberOrNull("scale")?.toInt() ?: 50,
            layers = visualLayers(),
        )

    private fun Map<String, Any?>.toAvatarFrame(): PersonalizationContent.AvatarFrame {
        val borderColor = HexColor(requireString("borderColor"))

        return PersonalizationContent.AvatarFrame(
            borderColor = borderColor,
            secondaryBorderColor = stringOrNull("secondaryBorderColor")?.let(::HexColor) ?: borderColor,
            borderWidthPx = requireNumber("borderWidthPx").toInt(),
            gapPx = numberOrNull("gapPx")?.toInt() ?: 0,
            radiusPx = requireNumber("radiusPx").toInt(),
            style = stringOrNull("style")?.let(FrameStyle::valueOf) ?: FrameStyle.SOLID,
            glow = documentOrNull("glow")?.toGlow(),
            layers = visualLayers(),
        )
    }

    private fun Map<String, Any?>.toProfileEffect(): PersonalizationContent.ProfileEffect =
        PersonalizationContent.ProfileEffect(
            effect = EffectType.valueOf(requireString("effect")),
            intensity = requireNumber("intensity").toInt(),
            primaryColor = HexColor(requireString("primaryColor")),
            secondaryColor = stringOrNull("secondaryColor")?.let(::HexColor),
            speed = numberOrNull("speed")?.toInt() ?: 50,
            density = numberOrNull("density")?.toInt() ?: 50,
            scale = numberOrNull("scale")?.toInt() ?: 50,
            layers = visualLayers(),
        )

    private fun Map<String, Any?>.toNotificationPack(): PersonalizationContent.NotificationPack {
        val templates = requireDocument(requireValue("templates"))

        return PersonalizationContent.NotificationPack(
            mixedAggregateTitle = requireString("mixedAggregateTitle"),
            templates =
                templates
                    .map { (rawKey, rawTemplate) ->
                        NotificationTemplateKey.valueOf(rawKey) to requireDocument(rawTemplate).toNotificationTemplate()
                    }.toMap(),
            valueMappings = optionalDocument("valueMappings").toValueMappings(),
        )
    }

    private fun Map<String, Any?>.toNotificationTemplate(): NotificationTemplate =
        NotificationTemplate(
            emoji = stringOrNull("emoji"),
            soloTitle = requireString("soloTitle"),
            soloBody = requireString("soloBody"),
            compact = requireString("compact"),
            aggregateTitle = requireString("aggregateTitle"),
            variants =
                optionalList("variants").map { rawVariant ->
                    requireDocument(rawVariant).toNotificationVariant()
                },
        )

    private fun Map<String, Any?>.toNotificationVariant(): NotificationTemplateVariant =
        NotificationTemplateVariant(
            id = requireString("id"),
            conditions =
                requireList("conditions").map { rawCondition ->
                    requireDocument(rawCondition).toNotificationCondition()
                },
            emoji = stringOrNull("emoji"),
            soloTitle = requireString("soloTitle"),
            soloBody = requireString("soloBody"),
            compact = requireString("compact"),
            aggregateTitle = requireString("aggregateTitle"),
        )

    private fun Map<String, Any?>.toNotificationCondition(): NotificationTemplateCondition =
        NotificationTemplateCondition(
            field = NotificationPackVariantField.valueOf(requireString("field")),
            value = requireString("value"),
        )

    private fun Map<String, Any?>.toValueMappings(): Map<NotificationPackValueKey, Map<String, NotificationValueForms>> =
        map { (rawKey, rawMappings) ->
            NotificationPackValueKey.valueOf(rawKey) to
                requireDocument(rawMappings).mapValues { (_, rawForms) ->
                    requireDocument(rawForms).toNotificationValueForms()
                }
        }.toMap()

    private fun Map<String, Any?>.toNotificationValueForms(): NotificationValueForms =
        NotificationValueForms(
            default = requireString("default"),
            forms =
                optionalDocument("forms").mapValues { (name, rawValue) ->
                    rawValue as? String
                        ?: throw IllegalArgumentException("Notification value form $name must be a string")
                },
        )

    private fun Map<NotificationPackValueKey, Map<String, NotificationValueForms>>.toDocument(): Map<String, Any?> =
        mapKeys { (key, _) -> key.name }
            .mapValues { (_, mappings) ->
                mappings.mapValues { (_, forms) -> forms.toDocument() }
            }

    private fun NotificationValueForms.toDocument(): Map<String, Any?> =
        mapOf(
            "default" to default,
            "forms" to forms,
        )

    private fun NotificationTemplate.toDocument(): Map<String, Any?> =
        mapOf(
            "emoji" to emoji,
            "soloTitle" to soloTitle,
            "soloBody" to soloBody,
            "compact" to compact,
            "aggregateTitle" to aggregateTitle,
            "variants" to variants.map { variant -> variant.toDocument() },
        )

    private fun NotificationTemplateVariant.toDocument(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "conditions" to conditions.map { condition -> condition.toDocument() },
            "emoji" to emoji,
            "soloTitle" to soloTitle,
            "soloBody" to soloBody,
            "compact" to compact,
            "aggregateTitle" to aggregateTitle,
        )

    private fun NotificationTemplateCondition.toDocument(): Map<String, Any?> =
        mapOf(
            "field" to field.name,
            "value" to value,
        )

    private fun Map<String, Any?>.visualLayers(): List<VisualLayer> =
        optionalList("layers").map { value ->
            requireDocument(value).toVisualLayer()
        }

    private fun VisualLayer.toDocument(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "assetId" to assetId.id.toString(),
            "xPercent" to xPercent,
            "yPercent" to yPercent,
            "widthPercent" to widthPercent,
            "opacity" to opacity,
            "rotationDegrees" to rotationDegrees,
            "blendMode" to blendMode.name,
            "animation" to animation.name,
            "zIndex" to zIndex,
        )

    private fun Map<String, Any?>.toVisualLayer(): VisualLayer =
        VisualLayer(
            id = requireString("id"),
            assetId = PersonalizationAssetId(UUID.fromString(requireString("assetId"))),
            xPercent = requireNumber("xPercent").toInt(),
            yPercent = requireNumber("yPercent").toInt(),
            widthPercent = requireNumber("widthPercent").toInt(),
            opacity = requireNumber("opacity").toInt(),
            rotationDegrees = requireNumber("rotationDegrees").toInt(),
            blendMode = VisualLayerBlendMode.valueOf(requireString("blendMode")),
            animation = VisualLayerAnimation.valueOf(requireString("animation")),
            zIndex = requireNumber("zIndex").toInt(),
        )

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

    private fun Map<String, Any?>.numberOrNull(key: String): Number? =
        when (val value = this[key]) {
            null -> null
            is Number -> value
            else -> throw IllegalArgumentException("Personalization content field $key must be a number")
        }

    private fun Map<String, Any?>.requireList(key: String): List<*> =
        requireValue(key) as? List<*>
            ?: throw IllegalArgumentException("Personalization content field $key must be a list")

    private fun Map<String, Any?>.optionalList(key: String): List<*> =
        when (val value = this[key]) {
            null -> emptyList<Any?>()
            is List<*> -> value
            else -> throw IllegalArgumentException("Personalization content field $key must be a list")
        }

    private fun Map<String, Any?>.documentOrNull(key: String): Map<String, Any?>? = this[key]?.let(::requireDocument)

    private fun Map<String, Any?>.optionalDocument(key: String): Map<String, Any?> =
        when (val value = this[key]) {
            null -> emptyMap()
            else -> requireDocument(value)
        }

    @Suppress("UNCHECKED_CAST")
    private fun requireDocument(value: Any?): Map<String, Any?> =
        value as? Map<String, Any?>
            ?: throw IllegalArgumentException("Personalization content value must be an object")
}
