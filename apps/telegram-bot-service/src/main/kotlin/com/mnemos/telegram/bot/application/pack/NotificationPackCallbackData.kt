package com.mnemos.telegram.bot.application.pack

import java.util.UUID

object NotificationPackCallbackData {
    const val PREFIX = "pk:"
    const val STORE = "pk:store"
    const val OWNED = "pk:owned"
    const val REFRESH = "pk:refresh"

    fun page(page: Int): String = "pk:p:${page.coerceAtLeast(0)}"

    fun item(personalizationId: UUID): String = "pk:i:$personalizationId"

    fun purchase(personalizationId: UUID): String = "pk:b:$personalizationId"

    fun apply(personalizationId: UUID): String = "pk:a:$personalizationId"

    fun selectedPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(PAGE_PREFIX) }
            ?.removePrefix(PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page >= 0 }

    fun selectedItem(data: String): UUID? = parseUuid(data, ITEM_PREFIX)

    fun purchaseTarget(data: String): UUID? = parseUuid(data, PURCHASE_PREFIX)

    fun applyTarget(data: String): UUID? = parseUuid(data, APPLY_PREFIX)

    private fun parseUuid(
        data: String,
        prefix: String,
    ): UUID? =
        data
            .takeIf { value -> value.startsWith(prefix) }
            ?.removePrefix(prefix)
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    private const val PAGE_PREFIX = "pk:p:"
    private const val ITEM_PREFIX = "pk:i:"
    private const val PURCHASE_PREFIX = "pk:b:"
    private const val APPLY_PREFIX = "pk:a:"
}
