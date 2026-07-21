package com.mnemos.common.core.model.user

@JvmInline
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username cannot be blank" }
        require(value.length in 3 .. 20) { "Username must be between 3 and 20 characters long" }
    }

    override fun toString() = value
}