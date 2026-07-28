package com.mnemos.account.domain.model.account.invite

import java.util.Locale

@JvmInline
value class InviteCode private constructor(
    val value: String,
) {
    companion object {
        internal const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        internal const val SYMBOL_COUNT = 16

        private const val GROUP_SIZE = 4

        fun of(raw: String): InviteCode {
            val symbols =
                raw
                    .trim()
                    .uppercase(Locale.ROOT)
                    .filterNot { it == '-' || it.isWhitespace() }

            require(symbols.length == SYMBOL_COUNT) {
                "Invite code must contain exactly $SYMBOL_COUNT symbols"
            }

            require(symbols.all { it in ALPHABET }) {
                "Invite code contains unsupported symbols"
            }

            return InviteCode(
                symbols.chunked(GROUP_SIZE).joinToString("-"),
            )
        }
    }
}
