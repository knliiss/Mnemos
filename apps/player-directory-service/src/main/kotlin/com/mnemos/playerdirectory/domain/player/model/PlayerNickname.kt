package com.mnemos.playerdirectory.domain.player.model

import java.util.Locale

@JvmInline
value class PlayerNickname private constructor(
    val value: String,
) {
    fun normalized(): String = value.lowercase(Locale.ROOT)

    override fun toString(): String = value

    companion object {
        private const val MAX_LENGTH = 64

        fun of(rawValue: String): PlayerNickname {
            val value = rawValue.trim()

            require(value.isNotEmpty()) { "Player nickname cannot be blank" }
            require(value.length <= MAX_LENGTH) { "Player nickname cannot exceed $MAX_LENGTH characters" }

            return PlayerNickname(value)
        }
    }
}
