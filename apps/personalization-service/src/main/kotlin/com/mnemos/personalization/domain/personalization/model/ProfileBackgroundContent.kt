package com.mnemos.personalization.domain.personalization.model

data class ProfileBackgroundContent(
    val effect: EffectType,
    val intensity: Int,
    val primaryColor: HexColor,
    val secondaryColor: HexColor?,
    val speed: Int = 50,
    val density: Int = 50,
    val scale: Int = 50,
    val layers: List<VisualLayer> = emptyList(),
) : PersonalizationContent {
    init {
        require(intensity in 1..100) { "Profile background intensity must be between 1 and 100" }
        require(speed in 1..100) { "Profile background speed must be between 1 and 100" }
        require(density in 1..100) { "Profile background density must be between 1 and 100" }
        require(scale in 1..100) { "Profile background scale must be between 1 and 100" }
        require(layers.size <= 8) { "Personalization can contain at most 8 visual layers" }
        require(layers.map(VisualLayer::id).distinct().size == layers.size) {
            "Personalization visual layer ids must be unique"
        }
    }
}
