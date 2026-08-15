package com.mnemos.personalization.domain.personalization.model

@JvmInline
value class HexColor(val value: String) {
    init {
        require(HEX.matches(value)) { "Color must use #RRGGBB format" }
    }

    private companion object {
        val HEX = Regex("^#[0-9A-Fa-f]{6}$")
    }
}
