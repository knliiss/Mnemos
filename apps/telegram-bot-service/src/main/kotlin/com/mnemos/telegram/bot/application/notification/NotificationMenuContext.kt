package com.mnemos.telegram.bot.application.notification

object NotificationMenuContext {
    private const val PAGE_KEY = "notifications.page"

    fun page(context: Map<String, String>): Int =
        context[PAGE_KEY]
            ?.toIntOrNull()
            ?.coerceAtLeast(0)
            ?: 0

    fun withPage(
        context: Map<String, String>,
        page: Int,
    ): Map<String, String> {
        val normalizedPage = page.coerceAtLeast(0)

        return if (normalizedPage == 0) {
            context - PAGE_KEY
        } else {
            context + (PAGE_KEY to normalizedPage.toString())
        }
    }
}
