package com.mnemos.telegram.bot.application.prime

object PrimePurchaseCallbackData {
    const val PREFIX = "prime:"
    const val REFRESH = "prime:refresh"

    private const val BUY_PREFIX = "prime:buy:"

    fun buy(packageId: String): String = "$BUY_PREFIX$packageId"

    fun packageId(callbackData: String): String? =
        callbackData
            .takeIf { value -> value.startsWith(BUY_PREFIX) }
            ?.removePrefix(BUY_PREFIX)
            ?.takeIf(String::isNotBlank)
}
