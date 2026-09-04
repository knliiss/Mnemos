package com.mnemos.common.core.model.user

@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username cannot be blank" }
        require(value.length in MIN_LENGTH..MAX_LENGTH) {
            "Username must be between $MIN_LENGTH and $MAX_LENGTH characters long"
        }
    }

    override fun toString() = value

    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 64
    }
}
