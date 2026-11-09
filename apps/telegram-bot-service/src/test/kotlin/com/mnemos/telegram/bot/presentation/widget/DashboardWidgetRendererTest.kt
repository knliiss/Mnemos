package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DashboardWidgetRendererTest {
    private val renderer = DashboardWidgetRenderer()

    @Test
    fun escapesExternallySuppliedAccountNameAndShowsSnapshot() {
        val document =
            renderer.render(
                DashboardWidgetView(
                    accountName = "<script>alert('x')</script>&",
                    accountCount = 3,
                    coin = 120,
                    prime = 20,
                    dailyStreak = 6,
                    dailyClaimed = false,
                ),
            )

        assertFalse(document.html.contains("<script>alert"))
        assertTrue(document.html.contains("&lt;script&gt;"))
        assertTrue(document.html.contains("Доступних акаунтів: 3"))
        assertTrue(document.html.contains(">120<"))
        assertTrue(document.html.contains(">20<"))
        assertTrue(document.html.contains("Нагорода доступна"))
        assertTrue(document.actionRows.flatten().any { it.callbackData == "w:open:events" })
    }

    @Test
    fun showsRegistrationGuidanceWithoutActiveAccount() {
        val document =
            renderer.render(
                DashboardWidgetView(
                    accountName = null,
                    accountCount = 0,
                    coin = null,
                    prime = null,
                    dailyStreak = null,
                    dailyClaimed = null,
                ),
            )

        assertTrue(document.html.contains("Акаунт не вибрано"))
        assertTrue(document.html.contains("Створіть акаунт"))
        assertTrue(document.html.contains("Відкрийте меню нагород"))
    }
}
