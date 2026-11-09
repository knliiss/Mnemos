package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.notifications.NotificationWidgetDrop
import com.mnemos.telegram.bot.application.widget.notifications.NotificationWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class NotificationWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = NotificationWidgetRenderer(json)

    @Test
    fun rendersInteractiveListWithoutInjectingLabelsIntoHtml() {
        val label = "<script>bad()</script>"
        val view =
            NotificationWidgetView(
                accountId = UUID.randomUUID(),
                page = 0,
                totalPages = 2,
                totalDrops = 7,
                pingEnabled = true,
                itemDrops = listOf(NotificationWidgetDrop(UUID.randomUUID(), label)),
                boosters = 1,
                dungeons = 2,
                globalEvents = 3,
                raidEnabled = false,
            )

        val document = renderer.render(view)

        assertTrue(document.allowQueries)
        assertTrue(document.html.contains("TelegramWidget.request"))
        assertTrue(!document.html.contains(label))
        assertEquals(label, json.readTree(document.chatData).path("items")[0].path("label").asText())
        assertTrue(document.chatData!!.toByteArray().size <= 1024)
    }

    @Test
    fun compactPaginationStateFitsTelegramDataLimit() {
        val view =
            NotificationWidgetView(
                accountId = UUID.randomUUID(),
                page = 7,
                totalPages = 30,
                totalDrops = 120,
                pingEnabled = true,
                itemDrops = (1..4).map { NotificationWidgetDrop(UUID.randomUUID(), "Персоналізований предмет ".repeat(2)) },
                boosters = 10,
                dungeons = 5,
                globalEvents = 4,
                raidEnabled = true,
            )

        assertTrue(renderer.state(view).toByteArray(Charsets.UTF_8).size <= 1024)
    }
}
