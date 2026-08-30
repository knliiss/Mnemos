package com.mnemos.telegram.bot.application.online

object SaoOnlineCallbackData {
    const val PREFIX = "on:"
    const val REFRESH = "on:refresh"
    const val REALM_LIST = "on:list"
    const val PREVIOUS_PAGE = "on:p:prev"
    const val NEXT_PAGE = "on:p:next"

    private const val REALM_PREFIX = "on:r:"

    fun realm(realmNumber: Int): String = "$REALM_PREFIX$realmNumber"

    fun realmNumber(callbackData: String): Int? =
        callbackData
            .takeIf { value -> value.startsWith(REALM_PREFIX) }
            ?.removePrefix(REALM_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { realmNumber -> realmNumber > 0 }
}
