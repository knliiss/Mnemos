package com.mnemos.personalization.domain.personalization.model

import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId

data class VisualLayer(
    val id: String,
    val assetId: PersonalizationAssetId,
    val xPercent: Int,
    val yPercent: Int,
    val widthPercent: Int,
    val opacity: Int,
    val rotationDegrees: Int,
    val blendMode: VisualLayerBlendMode,
    val animation: VisualLayerAnimation,
    val zIndex: Int,
) {
    init {
        require(ID.matches(id)) { "Visual layer id is invalid" }
        require(xPercent in -50..150) { "Visual layer x position must be between -50 and 150" }
        require(yPercent in -50..150) { "Visual layer y position must be between -50 and 150" }
        require(widthPercent in 5..200) { "Visual layer width must be between 5 and 200" }
        require(opacity in 1..100) { "Visual layer opacity must be between 1 and 100" }
        require(rotationDegrees in -180..180) { "Visual layer rotation must be between -180 and 180" }
        require(zIndex in 0..20) { "Visual layer z-index must be between 0 and 20" }
    }

    companion object {
        private val ID = Regex("^[a-zA-Z0-9_-]{1,48}$")
    }
}

enum class VisualLayerBlendMode {
    NORMAL,
    SCREEN,
    MULTIPLY,
    OVERLAY,
    SOFT_LIGHT,
}

enum class VisualLayerAnimation {
    NONE,
    FLOAT,
    PULSE,
    SPIN,
    DRIFT,
}
