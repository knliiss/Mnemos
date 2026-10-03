package com.mnemos.personalization.domain.personalization.model

import com.mnemos.personalization.contracts.shared.NotificationPackValueKey

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
        val backgroundSecondary: HexColor = background,
        val surfaceSecondary: HexColor = surface,
        val gradientAngleDegrees: Int = 145,
        val textureStrength: Int = 0,
        val layers: List<VisualLayer> = emptyList(),
    ) : PersonalizationContent {
        init {
            require(gradientAngleDegrees in 0..360) { "Theme gradient angle must be between 0 and 360" }
            require(textureStrength in 0..100) { "Theme texture strength must be between 0 and 100" }
            validateVisualLayers(layers)
        }
    }

    data class AvatarFrame(
        val borderColor: HexColor,
        val borderWidthPx: Int,
        val radiusPx: Int,
        val glow: GlowStyle?,
        val secondaryBorderColor: HexColor = borderColor,
        val gapPx: Int = 0,
        val style: FrameStyle = FrameStyle.SOLID,
        val layers: List<VisualLayer> = emptyList(),
    ) : PersonalizationContent {
        init {
            require(borderWidthPx in 1..8) { "Avatar frame border width must be between 1 and 8" }
            require(radiusPx in 0..32) { "Avatar frame radius must be between 0 and 32" }
            require(gapPx in 0..8) { "Avatar frame gap must be between 0 and 8" }
            validateVisualLayers(layers)
        }
    }

    data class ProfileEffect(
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
            require(intensity in 1..100) { "Profile effect intensity must be between 1 and 100" }
            require(speed in 1..100) { "Profile effect speed must be between 1 and 100" }
            require(density in 1..100) { "Profile effect density must be between 1 and 100" }
            require(scale in 1..100) { "Profile effect scale must be between 1 and 100" }
            validateVisualLayers(layers)
        }
    }

    data class NotificationPack(
        val mixedAggregateTitle: String,
        val templates: Map<NotificationTemplateKey, NotificationTemplate>,
        val valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueForms>> = emptyMap(),
    ) : PersonalizationContent {
        init {
            val requiredTemplates = NotificationTemplateKey.entries.toSet() - NotificationTemplateKey.PING
            require(templates.keys.containsAll(requiredTemplates)) {
                "Notification pack must define every required template"
            }

            validateStaticText(mixedAggregateTitle, "mixed aggregate title")
            validateValueMappings()

            templates.forEach { (key, template) ->
                validateTemplate(
                    key = key,
                    template = template,
                )
            }
        }

        private fun validateTemplate(
            key: NotificationTemplateKey,
            template: NotificationTemplate,
        ) {
            validateMessageFields(
                key = key,
                emoji = template.emoji,
                soloTitle = template.soloTitle,
                soloBody = template.soloBody,
                compact = template.compact,
                aggregateTitle = template.aggregateTitle,
            )

            require(template.variants.size <= MAX_VARIANTS_PER_TEMPLATE) {
                "Notification template $key can contain at most $MAX_VARIANTS_PER_TEMPLATE variants"
            }
            require(
                template.variants
                    .map(NotificationTemplateVariant::id)
                    .distinct()
                    .size == template.variants.size,
            ) {
                "Notification template $key variant ids must be unique"
            }

            template.variants.forEach { variant ->
                variant.conditions.forEach { condition ->
                    require(condition.field.templateType == key.contractType) {
                        "Notification variant condition ${condition.field} cannot be used for template $key"
                    }
                }

                validateMessageFields(
                    key = key,
                    emoji = variant.emoji,
                    soloTitle = variant.soloTitle,
                    soloBody = variant.soloBody,
                    compact = variant.compact,
                    aggregateTitle = variant.aggregateTitle,
                )
            }

            validateVariantAmbiguity(
                key = key,
                variants = template.variants,
            )
        }

        private fun validateMessageFields(
            key: NotificationTemplateKey,
            emoji: String?,
            soloTitle: String,
            soloBody: String,
            compact: String,
            aggregateTitle: String,
        ) {
            emoji?.let { value -> validateEmoji(value, key) }

            validateTemplateText(key, soloTitle)
            validateTemplateText(key, soloBody)
            validateTemplateText(key, compact)
            validateStaticText(aggregateTitle, "$key aggregate title")
        }

        private fun validateTemplateText(
            key: NotificationTemplateKey,
            template: String,
        ) {
            validateText(template, "Notification template")

            val placeholders = placeholderMatches(template)

            require(placeholders.all { placeholder -> placeholder.name in key.allowedPlaceholders }) {
                "Template $key contains unsupported placeholders"
            }

            placeholders
                .filter { placeholder -> placeholder.form != null && placeholder.form != DEFAULT_FORM }
                .forEach { placeholder ->
                    validateNamedForm(placeholder)
                }
        }

        private fun validateNamedForm(placeholder: Placeholder) {
            val valueKey =
                requireNotNull(NotificationPackValueKey.forPlaceholder(placeholder.name)) {
                    "Placeholder {${placeholder.name}.${placeholder.form}} does not support named forms"
                }
            val mappings =
                requireNotNull(valueMappings[valueKey]) {
                    "Notification value mappings for $valueKey are required by named placeholder form ${placeholder.form}"
                }

            valueKey.allowedValues.forEach { rawValue ->
                val forms =
                    requireNotNull(mappings[rawValue]) {
                        "Notification value mapping for $valueKey/$rawValue is required by named placeholder form ${placeholder.form}"
                    }

                require(forms.resolve(placeholder.form) != null) {
                    "Notification value mapping for $valueKey/$rawValue does not define form ${placeholder.form}"
                }
            }
        }

        private fun validateValueMappings() {
            valueMappings.forEach { (valueKey, mappings) ->
                require(mappings.keys.all { rawValue -> rawValue in valueKey.allowedValues }) {
                    "Notification value mappings for $valueKey contain unsupported raw values"
                }
            }
        }

        private fun validateVariantAmbiguity(
            key: NotificationTemplateKey,
            variants: List<NotificationTemplateVariant>,
        ) {
            variants
                .groupBy { variant -> variant.conditions.size }
                .values
                .forEach { sameSpecificity ->
                    sameSpecificity.forEachIndexed { index, left ->
                        sameSpecificity.drop(index + 1).forEach { right ->
                            require(!canMatchSameEvent(left, right)) {
                                "Notification template $key variants ${left.id} and ${right.id} are ambiguous"
                            }
                        }
                    }
                }
        }

        private fun canMatchSameEvent(
            left: NotificationTemplateVariant,
            right: NotificationTemplateVariant,
        ): Boolean {
            val leftConditions = left.conditions.associate { condition -> condition.field to condition.value }
            val rightConditions = right.conditions.associate { condition -> condition.field to condition.value }
            val sharedFields = leftConditions.keys.intersect(rightConditions.keys)

            return sharedFields.all { field -> leftConditions[field] == rightConditions[field] }
        }

        private fun validateEmoji(
            emoji: String,
            key: NotificationTemplateKey,
        ) {
            validateStaticText(emoji, "$key emoji")
            require(emoji.length <= 16) { "Notification $key emoji is too long" }
        }

        private fun validateStaticText(
            value: String,
            description: String,
        ) {
            validateText(value, "Notification $description")

            require(placeholderMatches(value).isEmpty()) {
                "Notification $description cannot contain placeholders"
            }
        }

        private fun validateText(
            value: String,
            description: String,
        ) {
            require(value.isNotBlank()) { "$description cannot be blank" }
            require(value.length <= MAX_TEXT_LENGTH) { "$description is too long" }
            require('<' !in value && '>' !in value) {
                "$description cannot contain raw Telegram HTML"
            }

            val valueWithoutPlaceholders = PLACEHOLDER.replace(value, "")

            require('{' !in valueWithoutPlaceholders && '}' !in valueWithoutPlaceholders) {
                "$description contains malformed placeholders"
            }
        }

        private fun placeholderMatches(value: String): List<Placeholder> =
            PLACEHOLDER
                .findAll(value)
                .map { match ->
                    Placeholder(
                        name = match.groupValues[1],
                        form = match.groupValues[2].ifBlank { null },
                    )
                }.toList()

        private data class Placeholder(
            val name: String,
            val form: String?,
        )

        private companion object {
            const val MAX_TEXT_LENGTH = 512
            const val MAX_VARIANTS_PER_TEMPLATE = 16
            const val DEFAULT_FORM = "default"

            val PLACEHOLDER = Regex("\\{([a-zA-Z][a-zA-Z0-9_]*)(?:\\.([a-zA-Z][a-zA-Z0-9_]*))?}")
        }
    }
}

private fun validateVisualLayers(layers: List<VisualLayer>) {
    require(layers.size <= 8) { "Personalization can contain at most 8 visual layers" }
    require(layers.map { layer -> layer.id }.distinct().size == layers.size) {
        "Personalization visual layer ids must be unique"
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

enum class FrameStyle {
    SOLID,
    DOUBLE,
    SEGMENTED,
}
