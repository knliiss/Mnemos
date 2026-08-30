package com.mnemos.telegram.bot.application.notification

import java.util.UUID

object NotificationCallbackData {
    const val PREFIX = "ntf:"
    const val REFRESH = "${PREFIX}r"

    private const val PAGE_PREFIX = "${PREFIX}p:"
    private const val DELETE_PREFIX = "${PREFIX}d:"

    fun page(page: Int): String = "$PAGE_PREFIX${page.coerceAtLeast(0)}"

    fun delete(subscriptionId: UUID): String = "$DELETE_PREFIX$subscriptionId"

    fun selectedPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(PAGE_PREFIX) }
            ?.removePrefix(PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page >= 0 }

    fun deletedSubscriptionId(data: String): UUID? =
        data
            .takeIf { value -> value.startsWith(DELETE_PREFIX) }
            ?.removePrefix(DELETE_PREFIX)
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }
}
