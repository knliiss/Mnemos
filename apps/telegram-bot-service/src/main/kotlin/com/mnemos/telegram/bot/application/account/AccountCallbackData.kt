package com.mnemos.telegram.bot.application.account

import java.util.UUID

object AccountCallbackData {
    const val PREFIX = "a:"
    const val REFRESH = "a:r"
    private const val SWITCH_PREFIX = "a:s:"

    fun switch(accountId: UUID): String = "$SWITCH_PREFIX$accountId"

    fun switchedAccountId(callbackData: String): UUID? {
        if (!callbackData.startsWith(SWITCH_PREFIX)) {
            return null
        }

        return runCatching {
            UUID.fromString(callbackData.removePrefix(SWITCH_PREFIX))
        }.getOrNull()
    }
}
