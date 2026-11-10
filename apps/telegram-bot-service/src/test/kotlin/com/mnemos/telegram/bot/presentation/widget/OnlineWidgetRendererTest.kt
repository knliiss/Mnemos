package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetPlayer
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetRealm
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

class OnlineWidgetRendererTest {
    private val mapper = ObjectMapper()
    private val renderer = OnlineWidgetRenderer(mapper)

    @Test
    fun playerNamesRemainInDataNotExecutableHtml() {
        val nickname = "<script>alert(1)</script>"
        val view =
            OnlineWidgetView(
                trackedOnline = 40,
                totalRealms = 1,
                selectedRealmNumber = 2,
                selectedRealmName = "SAO-2",
                selectedRealmPlayerCount = 40,
                generatedAt = "19:00 UTC",
                realms = emptyList(),
                players = listOf(OnlineWidgetPlayer(nickname)),
            )

        val result = renderer.render(view)

        assertTrue(result.allowQueries)
        assertTrue(result.html.contains("TelegramWidget.request"))
        assertTrue(!result.html.contains(nickname))
        assertEquals(nickname, mapper.readTree(result.chatData).path("players")[0].asText())
        assertTrue(result.actionRows.flatten().any { it.callbackData == "w:open:online" })
    }

    @Test
    fun cappedRealmOverviewFitsStateBudget() {
        val view =
            OnlineWidgetView(
                trackedOnline = 9999,
                totalRealms = 30,
                selectedRealmNumber = null,
                selectedRealmName = null,
                selectedRealmPlayerCount = null,
                generatedAt = "19:30 UTC",
                realms = (1..8).map { OnlineWidgetRealm(it, "Тестовий SAO-реалм-" + it, 999) },
                players = emptyList(),
            )

        val data = renderer.state(view)

        assertTrue(data.toByteArray(Charsets.UTF_8).size <= 1024)
        assertEquals(8, mapper.readTree(data).path("realms").size())
    }
}
