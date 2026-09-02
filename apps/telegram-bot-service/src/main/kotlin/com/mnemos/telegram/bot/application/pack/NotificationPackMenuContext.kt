package com.mnemos.telegram.bot.application.pack

import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuFeedback
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import java.util.UUID

object NotificationPackMenuContext {
    private const val MODE_KEY = "packs.mode"
    private const val PAGE_KEY = "packs.page"
    private const val SELECTED_KEY = "packs.selected"
    private const val FEEDBACK_KEY = "packs.feedback"

    fun mode(context: Map<String, String>): NotificationPackMenuMode =
        context[MODE_KEY]
            ?.let { value -> runCatching { NotificationPackMenuMode.valueOf(value) }.getOrNull() }
            ?: NotificationPackMenuMode.STORE

    fun page(context: Map<String, String>): Int =
        context[PAGE_KEY]
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0

    fun selectedPersonalizationId(context: Map<String, String>): UUID? =
        context[SELECTED_KEY]
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    fun feedback(context: Map<String, String>): NotificationPackMenuFeedback? =
        context[FEEDBACK_KEY]
            ?.let { value -> runCatching { NotificationPackMenuFeedback.valueOf(value) }.getOrNull() }

    fun store(
        context: Map<String, String>,
        page: Int = page(context),
    ): Map<String, String> =
        context
            .minus(SELECTED_KEY)
            .minus(FEEDBACK_KEY) +
            mapOf(
                MODE_KEY to NotificationPackMenuMode.STORE.name,
                PAGE_KEY to page.coerceAtLeast(0).toString(),
            )

    fun owned(context: Map<String, String>): Map<String, String> =
        context
            .minus(SELECTED_KEY)
            .minus(FEEDBACK_KEY) +
            (MODE_KEY to NotificationPackMenuMode.OWNED.name)

    fun detail(
        context: Map<String, String>,
        personalizationId: UUID,
    ): Map<String, String> =
        context
            .minus(FEEDBACK_KEY) +
            mapOf(
                MODE_KEY to NotificationPackMenuMode.DETAIL.name,
                SELECTED_KEY to personalizationId.toString(),
            )

    fun withFeedback(
        context: Map<String, String>,
        feedback: NotificationPackMenuFeedback,
    ): Map<String, String> = context + (FEEDBACK_KEY to feedback.name)

    fun clearFeedback(context: Map<String, String>): Map<String, String> = context.minus(FEEDBACK_KEY)
}
