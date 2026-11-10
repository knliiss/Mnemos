package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetEntry
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class ProfileWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = ProfileWidgetRenderer(json)

    @Test
    fun profileReactionStatusIsServerDataAndButtonsUseExplicitDesiredState() {
        val view = ProfileWidgetView(
            "Kirito", 0, 1, 1,
            emptyList(),
            selected = entry("Kirito"),
            reaction = ProfileReactionState.Available(ProfileReactionType.LIKE),
        )

        val document = renderer.render(view)
        val state = json.readTree(document.chatData)

        assertTrue(state.path("canReact").asBoolean())
        assertEquals("LIKE", state.path("reaction").asText())
        assertTrue(document.html.contains("w1:prof:react:"))
        assertTrue(document.html.contains("w1:prof:comments"))
    }

    @Test
    fun untrustedNicknameNeverBecomesExecutableHtml() {
        val nickname = "<script>alert('hi')</script>"
        val document = renderer.render(ProfileWidgetView("Kirito", 0, 1, 1, listOf(entry(nickname))))

        assertTrue(document.html.contains("TelegramWidget.request"))
        assertTrue(!document.html.contains(nickname))
        assertEquals(nickname, json.readTree(document.chatData).path("profiles")[0].path("nickname").asText())
    }

    @Test
    fun fourResultCardsStayWithinTelegramChatDataLimit() {
        val view = ProfileWidgetView(
            "Kirito", 0, 1, 4,
            List(4) { entry("Кіріто".repeat(6)) },
        )
        assertTrue(renderer.render(view).chatData!!.toByteArray(Charsets.UTF_8).size <= 1_024)
    }

    private fun entry(name: String) = ProfileWidgetEntry(
        UUID.randomUUID(), name, 24, 3, 15,
        ProfilePresenceStatus.HIDDEN, null, null, "2026-11-10",
    )
}
