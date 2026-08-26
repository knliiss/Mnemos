package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.domain.profile.model.ProfileAppearance
import java.util.UUID

object ProfileAppearancePersistenceMapper {
    fun ProfileAppearance.toEntity(): ProfileAppearanceEntity =
        ProfileAppearanceEntity(
            accountId = accountId,
            revision = revision,
            appearance =
                ProfileAppearanceDocument.Appearance(
                    nickStyle = nickStyle?.toDocument(),
                    profileTheme = profileTheme?.toDocument(),
                    avatarFrame = avatarFrame?.toDocument(),
                    profileEffect = profileEffect?.toDocument(),
                    profileBackground = profileBackground?.toDocument(),
                ),
            updatedAt = updatedAt,
        )

    fun ProfileAppearanceEntity.toDomain(): ProfileAppearance =
        ProfileAppearance(
            accountId = accountId,
            revision = revision,
            nickStyle = appearance.nickStyle?.toDomain(),
            profileTheme = appearance.profileTheme?.toDomain(),
            avatarFrame = appearance.avatarFrame?.toDomain(),
            profileEffect = appearance.profileEffect?.toDomain(),
            updatedAt = updatedAt,
            profileBackground = appearance.profileBackground?.toDomain(),
        )

    private fun ProfileAppearance.NickStyle.toDocument(): ProfileAppearanceDocument.NickStyle =
        ProfileAppearanceDocument.NickStyle(
            personalizationId = personalizationId.toString(),
            colors = colors.map { stop -> stop.toDocument() },
            angleDegrees = angleDegrees,
            glow = glow?.toDocument(),
        )

    private fun ProfileAppearanceDocument.NickStyle.toDomain(): ProfileAppearance.NickStyle =
        ProfileAppearance.NickStyle(
            personalizationId = UUID.fromString(personalizationId),
            colors = colors.map { stop -> stop.toDomain() },
            angleDegrees = angleDegrees,
            glow = glow?.toDomain(),
        )

    private fun ProfileAppearance.ProfileTheme.toDocument(): ProfileAppearanceDocument.ProfileTheme =
        ProfileAppearanceDocument.ProfileTheme(
            personalizationId = personalizationId.toString(),
            background = background,
            surface = surface,
            accent = accent,
            text = text,
            border = border,
            glow = glow?.toDocument(),
            backgroundSecondary = backgroundSecondary,
            surfaceSecondary = surfaceSecondary,
            gradientAngleDegrees = gradientAngleDegrees,
            textureStrength = textureStrength,
            layers = layers.map { layer -> layer.toDocument() },
        )

    private fun ProfileAppearanceDocument.ProfileTheme.toDomain(): ProfileAppearance.ProfileTheme =
        ProfileAppearance.ProfileTheme(
            personalizationId = UUID.fromString(personalizationId),
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
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearance.AvatarFrame.toDocument(): ProfileAppearanceDocument.AvatarFrame =
        ProfileAppearanceDocument.AvatarFrame(
            personalizationId = personalizationId.toString(),
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDocument(),
            secondaryBorderColor = secondaryBorderColor,
            gapPx = gapPx,
            style = style,
            layers = layers.map { layer -> layer.toDocument() },
        )

    private fun ProfileAppearanceDocument.AvatarFrame.toDomain(): ProfileAppearance.AvatarFrame =
        ProfileAppearance.AvatarFrame(
            personalizationId = UUID.fromString(personalizationId),
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDomain(),
            secondaryBorderColor = secondaryBorderColor,
            gapPx = gapPx,
            style = style,
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearance.ProfileEffect.toDocument(): ProfileAppearanceDocument.ProfileEffect =
        ProfileAppearanceDocument.ProfileEffect(
            personalizationId = personalizationId.toString(),
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toDocument() },
        )

    private fun ProfileAppearanceDocument.ProfileEffect.toDomain(): ProfileAppearance.ProfileEffect =
        ProfileAppearance.ProfileEffect(
            personalizationId = UUID.fromString(personalizationId),
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearance.VisualLayer.toDocument(): ProfileAppearanceDocument.VisualLayer =
        ProfileAppearanceDocument.VisualLayer(
            id = id,
            assetId = assetId.toString(),
            xPercent = xPercent,
            yPercent = yPercent,
            widthPercent = widthPercent,
            opacity = opacity,
            rotationDegrees = rotationDegrees,
            blendMode = blendMode,
            animation = animation,
            zIndex = zIndex,
        )

    private fun ProfileAppearanceDocument.VisualLayer.toDomain(): ProfileAppearance.VisualLayer =
        ProfileAppearance.VisualLayer(
            id = id,
            assetId = UUID.fromString(assetId),
            xPercent = xPercent,
            yPercent = yPercent,
            widthPercent = widthPercent,
            opacity = opacity,
            rotationDegrees = rotationDegrees,
            blendMode = blendMode,
            animation = animation,
            zIndex = zIndex,
        )

    private fun ProfileAppearance.GradientStop.toDocument(): ProfileAppearanceDocument.GradientStop =
        ProfileAppearanceDocument.GradientStop(
            color = color,
            position = position,
        )

    private fun ProfileAppearanceDocument.GradientStop.toDomain(): ProfileAppearance.GradientStop =
        ProfileAppearance.GradientStop(
            color = color,
            position = position,
        )

    private fun ProfileAppearance.Glow.toDocument(): ProfileAppearanceDocument.Glow =
        ProfileAppearanceDocument.Glow(
            color = color,
            strength = strength,
        )

    private fun ProfileAppearanceDocument.Glow.toDomain(): ProfileAppearance.Glow =
        ProfileAppearance.Glow(
            color = color,
            strength = strength,
        )
}
