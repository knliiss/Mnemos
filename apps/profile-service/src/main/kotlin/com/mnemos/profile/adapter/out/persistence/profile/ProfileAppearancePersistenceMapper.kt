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
        )

    private fun ProfileAppearance.AvatarFrame.toDocument(): ProfileAppearanceDocument.AvatarFrame =
        ProfileAppearanceDocument.AvatarFrame(
            personalizationId = personalizationId.toString(),
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDocument(),
        )

    private fun ProfileAppearanceDocument.AvatarFrame.toDomain(): ProfileAppearance.AvatarFrame =
        ProfileAppearance.AvatarFrame(
            personalizationId = UUID.fromString(personalizationId),
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDomain(),
        )

    private fun ProfileAppearance.ProfileEffect.toDocument(): ProfileAppearanceDocument.ProfileEffect =
        ProfileAppearanceDocument.ProfileEffect(
            personalizationId = personalizationId.toString(),
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
        )

    private fun ProfileAppearanceDocument.ProfileEffect.toDomain(): ProfileAppearance.ProfileEffect =
        ProfileAppearance.ProfileEffect(
            personalizationId = UUID.fromString(personalizationId),
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
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
