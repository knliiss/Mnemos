package com.mnemos.profile.adapter.personalization

import com.mnemos.personalization.contracts.shared.ProfileAppearanceSnapshotV1
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import java.time.Instant
import java.util.UUID

object ProfileAppearanceSnapshotMapper {
    fun ProfileAppearanceSnapshotV1.toDomain(
        accountId: UUID,
        revision: Long,
        updatedAt: Instant,
    ): ProfileAppearance =
        ProfileAppearance(
            accountId = accountId,
            revision = revision,
            nickStyle = nickStyle?.toDomain(),
            profileTheme = profileTheme?.toDomain(),
            profileBackground = profileBackground?.toDomain(),
            avatarFrame = avatarFrame?.toDomain(),
            profileEffect = profileEffect?.toDomain(),
            updatedAt = updatedAt,
        )

    private fun ProfileAppearanceSnapshotV1.NickStyle.toDomain(): ProfileAppearance.NickStyle =
        ProfileAppearance.NickStyle(
            personalizationId = personalizationId,
            colors = colors.map { stop -> stop.toDomain() },
            angleDegrees = angleDegrees,
            glow = glow?.toDomain(),
        )

    private fun ProfileAppearanceSnapshotV1.ProfileTheme.toDomain(): ProfileAppearance.ProfileTheme =
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
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearanceSnapshotV1.ProfileBackground.toDomain(): ProfileAppearance.ProfileBackground =
        ProfileAppearance.ProfileBackground(
            personalizationId = personalizationId,
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearanceSnapshotV1.AvatarFrame.toDomain(): ProfileAppearance.AvatarFrame =
        ProfileAppearance.AvatarFrame(
            personalizationId = personalizationId,
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDomain(),
            secondaryBorderColor = secondaryBorderColor,
            gapPx = gapPx,
            style = style,
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearanceSnapshotV1.ProfileEffect.toDomain(): ProfileAppearance.ProfileEffect =
        ProfileAppearance.ProfileEffect(
            personalizationId = personalizationId,
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toDomain() },
        )

    private fun ProfileAppearanceSnapshotV1.VisualLayer.toDomain(): ProfileAppearance.VisualLayer =
        ProfileAppearance.VisualLayer(
            id = id,
            assetId = assetId,
            xPercent = xPercent,
            yPercent = yPercent,
            widthPercent = widthPercent,
            opacity = opacity,
            rotationDegrees = rotationDegrees,
            blendMode = blendMode,
            animation = animation,
            zIndex = zIndex,
        )

    private fun ProfileAppearanceSnapshotV1.GradientStop.toDomain(): ProfileAppearance.GradientStop =
        ProfileAppearance.GradientStop(
            color = color,
            position = position,
        )

    private fun ProfileAppearanceSnapshotV1.Glow.toDomain(): ProfileAppearance.Glow =
        ProfileAppearance.Glow(
            color = color,
            strength = strength,
        )
}
