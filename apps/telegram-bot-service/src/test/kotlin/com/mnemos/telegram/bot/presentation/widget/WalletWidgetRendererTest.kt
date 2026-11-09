package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.wallet.WalletHistoryStatus
import com.mnemos.telegram.bot.application.widget.wallet.WalletTransactionEntry
import com.mnemos.telegram.bot.application.widget.wallet.WalletWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class WalletWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = WalletWidgetRenderer(json)

    @Test
    fun transactionLabelsStayInStateNotExecutableHtml() {
        val label = "<script>danger()</script>"
        val view =
            WalletWidgetView(
                accountId = UUID.randomUUID(),
                coin = 300,
                prime = 5,
                page = 0,
                totalPages = 1,
                totalTransactions = 1,
                historyStatus = WalletHistoryStatus.AVAILABLE,
                entries = listOf(WalletTransactionEntry(label, 100, "COIN", "2026-11-09 19:30")),
            )

        val widget = renderer.render(view)
        val state = json.readTree(widget.chatData)

        assertTrue(widget.allowQueries)
        assertTrue(widget.html.contains("TelegramWidget.request"))
        assertTrue(!widget.html.contains(label))
        assertEquals(label, state.path("entries")[0].path("label").asText())
        assertTrue(widget.actionRows.flatten().any { it.callbackData == "w:open:prime" })
    }

    @Test
    fun fourRecentWalletTransactionsFitTelegramStateBudget() {
        val view =
            WalletWidgetView(
                accountId = UUID.randomUUID(),
                coin = Long.MAX_VALUE,
                prime = 100,
                page = 10,
                totalPages = 12,
                totalTransactions = 48,
                historyStatus = WalletHistoryStatus.AVAILABLE,
                entries =
                    (1..4).map { index ->
                        WalletTransactionEntry("Нагорода за досягнення " + index, 9999999, "COIN", "2026-11-09 19:30")
                    },
            )

        assertTrue(renderer.state(view).toByteArray(Charsets.UTF_8).size <= 1024)
    }
}
