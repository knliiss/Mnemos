package com.mnemos.account.domain.model.account

@JvmInline
value class CristalixNickname(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Nickname cannot be blank" }
        require(value.length in 4..20) { "Nickname must be between 4 and 20 characters long" }
    }
}
