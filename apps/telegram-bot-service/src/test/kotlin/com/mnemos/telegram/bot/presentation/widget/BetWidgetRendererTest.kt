package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.widget.bets.BetWidgetEntry
import com.mnemos.telegram.bot.application.widget.bets.BetWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class BetWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = BetWidgetRenderer(json)

    @Test
    fun rendersBettingDashboardAndLeavesMoneyInputInExistingBot() {
        val view =
            BetWidgetView(
                accountId = UUID.randomUUID(),
                mode = BetMenuMode.ROUNDS,
                totalItems = 1,
                entries = listOf(BetWidgetEntry("<b>GLOBAL</b>", "YES 2.0 · NO 1.5", "OPEN")),
            )

        val result = renderer.render(view)
        val state = json.readTree(result.chatData)

        assertTrue(result.allowQueries)
        assertTrue(result.html.contains("w1:bet:mode:"))
        assertTrue(!result.html.contains("<b>GLOBAL</b>"))
        assertEquals("<b>GLOBAL</b>", state.path("entries")[0].path("title").asText())
        assertTrue(result.actionRows.flatten().any { it.callbackData == "w:open:bets" })
    }

    @Test
    fun fourCompactRowsFitTheStateLimit() {
        val view =
            BetWidgetView(
                accountId = UUID.randomUUID(),
                mode = BetMenuMode.HISTORY,
                totalItems = 40,
                entries =
                    List(4) {
                        BetWidgetEntry("Довгий_предмет_123", "Ставка: 999999 COIN", "Підтверджено")
                    },
            )

        assertTrue(renderer.state(view).toByteArray(Charsets.UTF_8).size <= 1024)
    }
}
