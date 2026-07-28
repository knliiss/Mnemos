package com.mnemos.accountservice.domain.model.account.role

@JvmInline
value class HexColor(
    val value: String,
) {
    init {
        require(value.matches(Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$"))) {
            "Invalid hex color: $value"
        }
    }
}
