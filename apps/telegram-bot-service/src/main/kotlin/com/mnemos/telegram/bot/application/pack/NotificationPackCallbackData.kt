package com.mnemos.telegram.bot.application.pack

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.telegram.bot.application.pack.model.NotificationPackAggregatePreviewMode
import java.util.UUID

object NotificationPackCallbackData {
    const val PREFIX = "pk:"
    const val STORE = "pk:store"
    const val OWNED = "pk:owned"
    const val REFRESH = "pk:refresh"
    const val BASE_VARIANT = "pk:vd"

    fun page(page: Int): String = "pk:p:${page.coerceAtLeast(0)}"

    fun item(personalizationId: UUID): String = "pk:i:$personalizationId"

    fun purchase(personalizationId: UUID): String = "pk:b:$personalizationId"

    fun apply(personalizationId: UUID): String = "pk:a:$personalizationId"

    fun template(type: NotificationPackTemplateType): String = "$TEMPLATE_PREFIX${type.name}"

    fun variant(variantId: String): String = "$VARIANT_PREFIX$variantId"

    fun aggregateMode(mode: NotificationPackAggregatePreviewMode): String = "$AGGREGATE_PREFIX${mode.name}"

    fun selectedPage(data: String): Int? =
        data
            .takeIf { value -> value.startsWith(PAGE_PREFIX) }
            ?.removePrefix(PAGE_PREFIX)
            ?.toIntOrNull()
            ?.takeIf { page -> page >= 0 }

    fun selectedItem(data: String): UUID? = parseUuid(data, ITEM_PREFIX)

    fun purchaseTarget(data: String): UUID? = parseUuid(data, PURCHASE_PREFIX)

    fun applyTarget(data: String): UUID? = parseUuid(data, APPLY_PREFIX)

    fun selectedTemplate(data: String): NotificationPackTemplateType? =
        data
            .takeIf { value -> value.startsWith(TEMPLATE_PREFIX) }
            ?.removePrefix(TEMPLATE_PREFIX)
            ?.let { value -> runCatching { NotificationPackTemplateType.valueOf(value) }.getOrNull() }

    fun selectedVariant(data: String): String? =
        data
            .takeIf { value -> value.startsWith(VARIANT_PREFIX) }
            ?.removePrefix(VARIANT_PREFIX)
            ?.takeIf { value -> VARIANT_ID.matches(value) }

    fun selectedAggregateMode(data: String): NotificationPackAggregatePreviewMode? =
        data
            .takeIf { value -> value.startsWith(AGGREGATE_PREFIX) }
            ?.removePrefix(AGGREGATE_PREFIX)
            ?.let { value -> runCatching { NotificationPackAggregatePreviewMode.valueOf(value) }.getOrNull() }

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
    private const val TEMPLATE_PREFIX = "pk:t:"
    private const val VARIANT_PREFIX = "pk:v:"
    private const val AGGREGATE_PREFIX = "pk:g:"

    private val VARIANT_ID = Regex("^[a-zA-Z0-9_-]{1,48}$")
}
