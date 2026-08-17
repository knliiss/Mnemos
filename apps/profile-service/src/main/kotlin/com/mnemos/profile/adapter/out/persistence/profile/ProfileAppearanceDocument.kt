package com.mnemos.profile.adapter.out.persistence.profile

object ProfileAppearanceDocument {
    data class Appearance(
        val nickStyle: NickStyle?,
        val profileTheme: ProfileTheme?,
        val avatarFrame: AvatarFrame?,
        val profileEffect: ProfileEffect?,
    )

    data class NickStyle(
        val personalizationId: String,
        val colors: List<GradientStop>,
        val angleDegrees: Int,
        val glow: Glow?,
    )

    data class ProfileTheme(
        val personalizationId: String,
        val background: String,
        val surface: String,
        val accent: String,
        val text: String,
        val border: String,
        val glow: Glow?,
    )

    data class AvatarFrame(
        val personalizationId: String,
        val borderColor: String,
        val borderWidthPx: Int,
        val radiusPx: Int,
        val glow: Glow?,
    )

    data class ProfileEffect(
        val personalizationId: String,
        val effect: String,
        val intensity: Int,
        val primaryColor: String,
        val secondaryColor: String?,
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
