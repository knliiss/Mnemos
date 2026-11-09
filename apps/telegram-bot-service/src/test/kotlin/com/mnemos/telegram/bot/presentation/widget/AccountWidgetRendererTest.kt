package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetEntry
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class AccountWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = AccountWidgetRenderer(json)

    @Test
    fun accountNamesStayInStateNotExecutableHtml() {
        val name = "<script>danger()</script>"
        val id = UUID.randomUUID()
        val view =
            AccountWidgetView(
                activeAccountId = id,
                accounts = listOf(AccountWidgetEntry(id, name, true, 3, 5, true, true)),
                totalAccounts = 1,
                page = 0,
                totalPages = 1,
                coin = 10,
                prime = 2,
            )

        val widget = renderer.render(view)
        val state = json.readTree(widget.chatData)

        assertEquals(name, state.path("accounts")[0].path("name").asText())
        assertTrue(!widget.html.contains(name))
        assertTrue(widget.html.contains("TelegramWidget.request"))
        assertTrue(widget.html.contains("w1:acct:select:"))
        assertTrue(widget.allowQueries)
    }

    @Test
    fun stateFitsTheTelegramLimitForFourAccountsWithLongNames() {
        val view =
            AccountWidgetView(
                activeAccountId = null,
                accounts =
                    (1..4).map {
                        AccountWidgetEntry(UUID.randomUUID(), "ПерсонажІзДовгимІменем", false, 3, 10, false, true)
                    },
                totalAccounts = 99,
                page = 4,
                totalPages = 25,
                coin = null,
                prime = null,
            )

        assertTrue(renderer.state(view).toByteArray(Charsets.UTF_8).size <= 1024)
        assertEquals(4, json.readTree(renderer.state(view)).path("accounts").size())
    }
}
