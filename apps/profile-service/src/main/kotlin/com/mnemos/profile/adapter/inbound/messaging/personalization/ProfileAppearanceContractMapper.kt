package com.mnemos.profile.adapter.inbound.messaging.personalization

import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import com.mnemos.profile.domain.profile.model.ProfileAppearance

object ProfileAppearanceContractMapper {
    fun ProfileAppearanceChangedV1.toDomain(): ProfileAppearance =
        ProfileAppearance(
            accountId = accountId,
            revision = revision,
            nickStyle = appearance.nickStyle?.toDomain(),
            profileTheme = appearance.profileTheme?.toDomain(),
            avatarFrame = appearance.avatarFrame?.toDomain(),
            profileEffect = appearance.profileEffect?.toDomain(),
            updatedAt = occurredAt,
        )

    private fun ProfileAppearanceChangedV1.NickStyle.toDomain(): ProfileAppearance.NickStyle =
        ProfileAppearance.NickStyle(
            personalizationId = personalizationId,
            colors = colors.map { stop -> stop.toDomain() },
            angleDegrees = angleDegrees,
            glow = glow?.toDomain(),
        )

    private fun ProfileAppearanceChangedV1.ProfileTheme.toDomain(): ProfileAppearance.ProfileTheme =
        ProfileAppearance.ProfileTheme(
            personalizationId = personalizationId,
            background = background,
            surface = surface,
            accent = accent,
            text = text,
            border = border,
            glow = glow?.toDomain(),
            backgroundSecondary = backgroundSecondary,
            surfaceSecondary = surfaceSecondary,
            gradientAngleDegrees = gradientAngleDegrees,
            textureStrength = textureStrength,
        )

    private fun ProfileAppearanceChangedV1.AvatarFrame.toDomain(): ProfileAppearance.AvatarFrame =
        ProfileAppearance.AvatarFrame(
            personalizationId = personalizationId,
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDomain(),
            secondaryBorderColor = secondaryBorderColor,
            gapPx = gapPx,
            style = style,
        )

    private fun ProfileAppearanceChangedV1.ProfileEffect.toDomain(): ProfileAppearance.ProfileEffect =
        ProfileAppearance.ProfileEffect(
            personalizationId = personalizationId,
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
        )

    private fun ProfileAppearanceChangedV1.GradientStop.toDomain(): ProfileAppearance.GradientStop =
        ProfileAppearance.GradientStop(
            color = color,
            position = position,
        )

    private fun ProfileAppearanceChangedV1.Glow.toDomain(): ProfileAppearance.Glow =
        ProfileAppearance.Glow(
            color = color,
            strength = strength,
        )
}
