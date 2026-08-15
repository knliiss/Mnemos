package com.mnemos.personalization.domain.personalization.model

sealed interface PersonalizationContent {
    data class NickStyle(
        val colors: List<GradientStop>,
        val angleDegrees: Int,
        val glow: GlowStyle?,
    ) : PersonalizationContent {
        init {
            require(colors.size in 1..4) { "Nick style must contain between 1 and 4 colors" }
            require(angleDegrees in 0..360) { "Gradient angle must be between 0 and 360" }
            require(colors.map { it.position }.distinct().size == colors.size) { "Gradient stop positions must be unique" }
        }
    }

    data class ProfileTheme(
        val background: HexColor,
        val surface: HexColor,
        val accent: HexColor,
        val text: HexColor,
        val border: HexColor,
        val glow: GlowStyle?,
    ) : PersonalizationContent

    data class AvatarFrame(
        val borderColor: HexColor,
        val borderWidthPx: Int,
        val radiusPx: Int,
        val glow: GlowStyle?,
    ) : PersonalizationContent {
        init {
            require(borderWidthPx in 1..8) { "Avatar frame border width must be between 1 and 8" }
            require(radiusPx in 0..32) { "Avatar frame radius must be between 0 and 32" }
        }
    }

    data class ProfileEffect(
        val effect: EffectType,
        val intensity: Int,
        val primaryColor: HexColor,
        val secondaryColor: HexColor?,
    ) : PersonalizationContent {
        init {
            require(intensity in 1..100) { "Profile effect intensity must be between 1 and 100" }
        }
    }

    data class NotificationPack(
        val templates: Map<NotificationTemplateKey, NotificationTemplate>,
    ) : PersonalizationContent {
        init {
            require(templates.keys.containsAll(NotificationTemplateKey.entries)) {
                "Notification pack must define every supported template"
            }

            templates.forEach { (key, template) ->
                validateTemplate(key, template.soloTitle)
                validateTemplate(key, template.soloBody)
                validateTemplate(key, template.compact)
            }
        }

        private fun validateTemplate(
            key: NotificationTemplateKey,
            template: String,
        ) {
            require(template.isNotBlank()) { "Notification template cannot be blank" }
            require(template.length <= 512) { "Notification template is too long" }

            val placeholders =
                PLACEHOLDER
                    .findAll(template)
                    .map { it.groupValues[1] }
                    .toSet()

            require(placeholders.all { it in key.allowedPlaceholders }) {
                "Template $key contains unsupported placeholders"
            }
        }

        private companion object {
            val PLACEHOLDER = Regex("\\{([a-zA-Z][a-zA-Z0-9_]*)}")
        }
    }
}

data class GradientStop(
    val color: HexColor,
    val position: Int,
) {
    init {
        require(position in 0..100) { "Gradient stop position must be between 0 and 100" }
    }
}

data class GlowStyle(
    val color: HexColor,
    val strength: Int,
) {
    init {
        require(strength in 1..100) { "Glow strength must be between 1 and 100" }
    }
}

enum class EffectType {
    AURORA,
    PULSE,
    SHIMMER,
    PARTICLES,
}

data class NotificationTemplate(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
)

enum class NotificationTemplateKey(
    val allowedPlaceholders: Set<String>,
) {
    ITEM_DROP(setOf("player", "item", "rarity", "itemType")),
    BOOSTER(setOf("player", "booster")),
    DUNGEON(setOf("dungeon")),
    RAID(setOf("locations")),
    GLOBAL(setOf("event")),
}
