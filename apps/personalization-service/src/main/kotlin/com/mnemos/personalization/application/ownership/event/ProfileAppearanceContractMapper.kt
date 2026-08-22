package com.mnemos.personalization.application.ownership.event

import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent

object ProfileAppearanceContractMapper {
    fun Personalization.toNickStyleSnapshot(): ProfileAppearanceChangedV1.NickStyle {
        val style =
            content as? PersonalizationContent.NickStyle
                ?: error("Nick style personalization must contain nick style content")

        return ProfileAppearanceChangedV1.NickStyle(
            personalizationId = id.id,
            colors = style.colors.map { stop -> stop.toContract() },
            angleDegrees = style.angleDegrees,
            glow = style.glow?.toContract(),
        )
    }

    fun Personalization.toProfileThemeSnapshot(): ProfileAppearanceChangedV1.ProfileTheme {
        val theme =
            content as? PersonalizationContent.ProfileTheme
                ?: error("Profile theme personalization must contain profile theme content")

        return ProfileAppearanceChangedV1.ProfileTheme(
            personalizationId = id.id,
            background = theme.background.value,
            surface = theme.surface.value,
            accent = theme.accent.value,
            text = theme.text.value,
            border = theme.border.value,
            glow = theme.glow?.toContract(),
            backgroundSecondary = theme.backgroundSecondary.value,
            surfaceSecondary = theme.surfaceSecondary.value,
            gradientAngleDegrees = theme.gradientAngleDegrees,
            textureStrength = theme.textureStrength,
        )
    }

    fun Personalization.toAvatarFrameSnapshot(): ProfileAppearanceChangedV1.AvatarFrame {
        val frame =
            content as? PersonalizationContent.AvatarFrame
                ?: error("Avatar frame personalization must contain avatar frame content")

        return ProfileAppearanceChangedV1.AvatarFrame(
            personalizationId = id.id,
            borderColor = frame.borderColor.value,
            borderWidthPx = frame.borderWidthPx,
            radiusPx = frame.radiusPx,
            glow = frame.glow?.toContract(),
            secondaryBorderColor = frame.secondaryBorderColor.value,
            gapPx = frame.gapPx,
            style = frame.style.name,
        )
    }

    fun Personalization.toProfileEffectSnapshot(): ProfileAppearanceChangedV1.ProfileEffect {
        val profileEffect =
            content as? PersonalizationContent.ProfileEffect
                ?: error("Profile effect personalization must contain profile effect content")

        return ProfileAppearanceChangedV1.ProfileEffect(
            personalizationId = id.id,
            effect = profileEffect.effect.name,
            intensity = profileEffect.intensity,
            primaryColor = profileEffect.primaryColor.value,
            secondaryColor = profileEffect.secondaryColor?.value,
            speed = profileEffect.speed,
            density = profileEffect.density,
            scale = profileEffect.scale,
        )
    }

    private fun GradientStop.toContract(): ProfileAppearanceChangedV1.GradientStop =
        ProfileAppearanceChangedV1.GradientStop(
            color = color.value,
            position = position,
        )

    private fun GlowStyle.toContract(): ProfileAppearanceChangedV1.Glow =
        ProfileAppearanceChangedV1.Glow(
            color = color.value,
            strength = strength,
        )
}
