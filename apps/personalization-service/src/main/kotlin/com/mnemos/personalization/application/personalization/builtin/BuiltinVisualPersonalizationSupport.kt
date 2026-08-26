package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.domain.personalization.model.EffectType
import com.mnemos.personalization.domain.personalization.model.FrameStyle
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinVisualPersonalizationSupport {
    fun nickStyle(
        key: String,
        name: String,
        colors: List<GradientStop>,
        angleDegrees: Int,
        glow: GlowStyle?,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition {
        return definition(
            key = key,
            type = PersonalizationType.NICK_STYLE,
            name = name,
            content =
                PersonalizationContent.NickStyle(
                    colors = colors,
                    angleDegrees = angleDegrees,
                    glow = glow,
                ),
            currency = currency,
            amount = amount,
        )
    }

    fun profileTheme(
        key: String,
        name: String,
        background: String,
        backgroundSecondary: String,
        surface: String,
        surfaceSecondary: String,
        accent: String,
        text: String,
        border: String,
        glow: GlowStyle?,
        angle: Int,
        texture: Int,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition {
        return definition(
            key = key,
            type = PersonalizationType.PROFILE_THEME,
            name = name,
            content =
                PersonalizationContent.ProfileTheme(
                    background = color(background),
                    backgroundSecondary = color(backgroundSecondary),
                    surface = color(surface),
                    surfaceSecondary = color(surfaceSecondary),
                    accent = color(accent),
                    text = color(text),
                    border = color(border),
                    glow = glow,
                    gradientAngleDegrees = angle,
                    textureStrength = texture,
                ),
            currency = currency,
            amount = amount,
        )
    }

    fun avatarFrame(
        key: String,
        name: String,
        primary: String,
        secondary: String,
        width: Int,
        radius: Int,
        gap: Int,
        style: FrameStyle,
        glow: GlowStyle?,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition {
        return definition(
            key = key,
            type = PersonalizationType.AVATAR_FRAME,
            name = name,
            content =
                PersonalizationContent.AvatarFrame(
                    borderColor = color(primary),
                    secondaryBorderColor = color(secondary),
                    borderWidthPx = width,
                    radiusPx = radius,
                    gapPx = gap,
                    style = style,
                    glow = glow,
                ),
            currency = currency,
            amount = amount,
        )
    }

    fun profileEffect(
        key: String,
        name: String,
        type: EffectType,
        intensity: Int,
        primary: String,
        secondary: String?,
        speed: Int,
        density: Int,
        scale: Int,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition {
        return definition(
            key = key,
            type = PersonalizationType.PROFILE_EFFECT,
            name = name,
            content =
                PersonalizationContent.ProfileEffect(
                    effect = type,
                    intensity = intensity,
                    primaryColor = color(primary),
                    secondaryColor = secondary?.let(::color),
                    speed = speed,
                    density = density,
                    scale = scale,
                ),
            currency = currency,
            amount = amount,
        )
    }

    fun stop(
        colorValue: String,
        position: Int,
    ): GradientStop {
        return GradientStop(
            color = color(colorValue),
            position = position,
        )
    }

    fun glow(
        colorValue: String,
        strength: Int,
    ): GlowStyle {
        return GlowStyle(
            color = color(colorValue),
            strength = strength,
        )
    }

    private fun definition(
        key: String,
        type: PersonalizationType,
        name: String,
        content: PersonalizationContent,
        currency: Currency,
        amount: Long,
    ): BuiltinPersonalizationDefinition {
        return BuiltinPersonalizationDefinition(
            catalogKey = PersonalizationCatalogKey(key),
            type = type,
            name = name,
            content = content,
            price =
                PersonalizationPrice(
                    currency = currency,
                    amount = amount,
                ),
        )
    }

    private fun color(value: String): HexColor = HexColor(value)
}
