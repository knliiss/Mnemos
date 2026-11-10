package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.profileactivity.ProfileActivityItem
import com.mnemos.telegram.bot.application.widget.profileactivity.ProfileActivityView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class ProfileActivityWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = ProfileActivityWidgetRenderer(json)

    @Test
    fun maximumPageUsesCompactFieldsAndFitsTelegramChatData() {
        val items = List(8) { ProfileActivityItem("К".repeat(18), "MYTHICAL", "2026-11-10 12:03") }
        val view = ProfileActivityView(UUID.randomUUID(), "Кіріто", null, UUID.randomUUID(), items)

        val output = renderer.render(view)

        assertTrue(output.chatData!!.toByteArray(Charsets.UTF_8).size <= 1_024)
        val first = json.readTree(output.chatData).path("drops")[0]
        assertEquals("К".repeat(18), first.path("n").asText())
        assertEquals("MYTHICAL", first.path("r").asText())
    }

    @Test
    fun serverNameIsNotInjectedIntoWidgetHtml() {
        val name = "<script>alert(1)</script>"
        val view = ProfileActivityView(
            UUID.randomUUID(),
            name,
            null,
            null,
            listOf(ProfileActivityItem(name, "SECRET", "2026-11-10 12:03")),
        )

        val rendered = renderer.render(view)

        assertFalse(rendered.html.contains(name))
        assertTrue(rendered.html.contains("TelegramWidget.request"))
    }
}
