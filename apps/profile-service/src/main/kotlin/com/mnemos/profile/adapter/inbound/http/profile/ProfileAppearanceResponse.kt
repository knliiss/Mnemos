package com.mnemos.profile.adapter.inbound.http.profile

import java.util.UUID

data class ProfileAppearanceResponse(
    val nickStyle: NickStyle?,
    val profileTheme: ProfileTheme?,
    val avatarFrame: AvatarFrame?,
    val profileEffect: ProfileEffect?,
    val profileBackground: ProfileEffect? = null,
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
        val backgroundSecondary: String,
        val surfaceSecondary: String,
        val gradientAngleDegrees: Int,
        val textureStrength: Int,
        val layers: List<VisualLayer>,
    )

    data class AvatarFrame(
        val personalizationId: UUID,
        val borderColor: String,
        val borderWidthPx: Int,
        val radiusPx: Int,
        val glow: Glow?,
        val secondaryBorderColor: String,
        val gapPx: Int,
        val style: String,
        val layers: List<VisualLayer>,
    )

    data class ProfileEffect(
        val personalizationId: UUID,
        val effect: String,
        val intensity: Int,
        val primaryColor: String,
        val secondaryColor: String?,
        val speed: Int,
        val density: Int,
        val scale: Int,
        val layers: List<VisualLayer>,
    )

    data class VisualLayer(
        val id: String,
        val assetId: UUID,
        val assetUrl: String,
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
}
