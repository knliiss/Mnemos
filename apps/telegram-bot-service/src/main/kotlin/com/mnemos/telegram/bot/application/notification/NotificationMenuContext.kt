package com.mnemos.telegram.bot.application.notification

object NotificationMenuContext {
    private const val PAGE_KEY = "notifications.page"

    fun page(context: Map<String, String>): Int =
        context[PAGE_KEY]
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0

    fun next(context: Map<String, String>): Map<String, String> =
        context + (PAGE_KEY to (page(context) + 1).toString())

    fun previous(context: Map<String, String>): Map<String, String> {
        val previousPage = (page(context) - 1).coerceAtLeast(0)

        return if (previousPage == 0) {
            context - PAGE_KEY
        } else {
            context + (PAGE_KEY to previousPage.toString())
        }
    }
}
