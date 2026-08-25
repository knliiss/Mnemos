package com.mnemos.personalization.domain.personalization.model

@JvmInline
value class PersonalizationCatalogKey(
    val value: String,
) {
    init {
        require(PATTERN.matches(value)) {
            "Personalization catalog key must contain lowercase letters, numbers, dots and dashes"
        }
    }

    companion object {
        private val PATTERN = Regex("^[a-z0-9][a-z0-9.-]{2,95}$")
    }
}
