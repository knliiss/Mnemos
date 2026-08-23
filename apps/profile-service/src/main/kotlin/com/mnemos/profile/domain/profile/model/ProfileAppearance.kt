package com.mnemos.profile.domain.profile.model

import java.time.Instant
import java.util.UUID

data class ProfileAppearance(
    val accountId: UUID,
    val revision: Long,
    val nickStyle: NickStyle?,
    val profileTheme: ProfileTheme?,
    val avatarFrame: AvatarFrame?,
    val profileEffect: ProfileEffect?,
    val updatedAt: Instant,
) {
    data class NickStyle(
        val personalizationId: UUID,
        val colors: List<GradientStop>,
        val angleDegrees: Int,
        val glow: Glow?,
    )

    data class ProfileTheme(
        val personalizationId: UUID,
        val background: String,
        val surface: String,
        val accent: String,
        val text: String,
        val border: String,
        val glow: Glow?,
        val backgroundSecondary: String = background,
        val surfaceSecondary: String = surface,
        val gradientAngleDegrees: Int = 145,
        val textureStrength: Int = 0,
        val layers: List<VisualLayer> = emptyList(),
    )

    data class AvatarFrame(
        val personalizationId: UUID,
        val borderColor: String,
        val borderWidthPx: Int,
        val radiusPx: Int,
        val glow: Glow?,
        val secondaryBorderColor: String = borderColor,
        val gapPx: Int = 0,
        val style: String = "SOLID",
        val layers: List<VisualLayer> = emptyList(),
    )

    data class ProfileEffect(
        val personalizationId: UUID,
        val effect: String,
        val intensity: Int,
        val primaryColor: String,
        val secondaryColor: String?,
        val speed: Int = 50,
        val density: Int = 50,
        val scale: Int = 50,
        val layers: List<VisualLayer> = emptyList(),
    )

    data class VisualLayer(
        val id: String,
        val assetId: UUID,
        val xPercent: Int,
        val yPercent: Int,
        val widthPercent: Int,
        val opacity: Int,
        val rotationDegrees: Int,
        val blendMode: String,
        val animation: String,
        val zIndex: Int,
    )

    data class GradientStop(
        val color: String,
        val position: Int,
    )

    data class Glow(
        val color: String,
        val strength: Int,
    )

    companion object {
        fun empty(accountId: UUID): ProfileAppearance =
            ProfileAppearance(
                accountId = accountId,
                revision = 0,
                nickStyle = null,
                profileTheme = null,
                avatarFrame = null,
                profileEffect = null,
                updatedAt = Instant.EPOCH,
            )
    }
}
