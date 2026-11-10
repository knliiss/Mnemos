package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.widget.events.EventWidgetItem
import com.mnemos.telegram.bot.application.widget.events.EventWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class EventWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = EventWidgetRenderer(json)

    @Test
    fun rendersFiltersAndEventsAsBoundedData() {
        val cursor = UUID.randomUUID()
        val view =
            EventWidgetView(
                filter = EventHistoryFilter.BOOSTER,
                after = null,
                nextCursor = cursor,
                items =
                    (1..8).map { index ->
                        EventWidgetItem("Бустер", "Подія номер $index", "19:45 UTC")
                    },
            )

        val widget = renderer.render(view)

        assertTrue(widget.allowQueries)
        assertTrue(widget.html.contains("TelegramWidget.request"))
        assertTrue(widget.html.contains("w1:evt:filter:"))
        assertTrue(widget.chatData!!.toByteArray(Charsets.UTF_8).size <= 1024)
        val state = json.readTree(widget.chatData)
        assertEquals("BOOSTER", state.path("filter").asText())
        assertEquals(cursor.toString(), state.path("cursor").asText())
        assertEquals(8, state.path("items").size())
    }

    @Test
    fun doesNotPlaceEventContentInsideHtmlCode() {
        val malicious = "<script>malicious()</script>"
        val widget =
            renderer.render(
                EventWidgetView(
                    filter = EventHistoryFilter.ALL,
                    after = null,
                    nextCursor = null,
                    items = listOf(EventWidgetItem("Дроп", malicious, "12:00 UTC")),
                ),
            )

        assertTrue(!widget.html.contains(malicious))
        assertEquals(malicious, json.readTree(widget.chatData).path("items")[0].path("label").asText())
    }
}
