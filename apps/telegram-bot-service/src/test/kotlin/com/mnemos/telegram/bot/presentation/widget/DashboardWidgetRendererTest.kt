package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class DashboardWidgetRendererTest {
    private val renderer = DashboardWidgetRenderer(ObjectMapper())
    private val json = ObjectMapper()

    @Test
    fun widgetIsInteractiveAndEscapesAccountNameInState() {
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

        val state = json.readTree(document.chatData)
        assertEquals("<script>alert('x')</script>&", state.path("name").asText())
        assertEquals(120, state.path("coin").asInt())
        assertEquals(20, state.path("prime").asInt())
        assertTrue(document.html.contains("TelegramWidget.request"))
        assertTrue(document.html.contains("chatDataChanged"))
        assertTrue(!document.html.contains("<script>alert('x')</script>&"))
        assertTrue(document.allowQueries)
        assertTrue(document.actionRows.flatten().any { it.callbackData == "w:open:events" })
    }

    @Test
    fun registrationGuidanceIsAvailableWithoutAccount() {
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

        val state = json.readTree(document.chatData)
        assertTrue(state.path("name").isNull)
        assertTrue(state.path("claimed").isNull)
        assertTrue(document.html.contains("Акаунт не вибрано"))
    }
}
