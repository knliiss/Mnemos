package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.profilecomments.ProfileCommentWidgetItem
import com.mnemos.telegram.bot.application.widget.profilecomments.ProfileCommentsWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class ProfileCommentsWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = ProfileCommentsWidgetRenderer(json)

    @Test
    fun untrustedCommentNeverEntersWidgetHtmlOrJavascript() {
        val payload = "<script>alert('x')</script>"
        val document = renderer.render(view(listOf(comment(content = payload))))

        assertFalse(document.html.contains(payload))
        assertTrue(document.html.contains("textContent"))
        assertEquals(payload, json.readTree(document.chatData).path("comments")[0].path("c").asText())
    }

    @Test
    fun longCyrillicCommentsFitTelegramChatDataAndNoRawIdsLeak() {
        val comments = List(3) {
            comment(content = "Ж".repeat(80), author = "А".repeat(13))
        }
        val document = renderer.render(view(comments))
        val data = json.readTree(document.chatData)

        assertTrue(document.allowQueries)
        assertTrue(document.chatData!!.toByteArray(Charsets.UTF_8).size <= 1_024)
        assertEquals(3, data.path("comments").size())
        assertEquals(12, data.path("comments")[0].path("t").asText().length)
        assertFalse(document.chatData!!.contains(comments.first().id.toString()))
    }

    private fun view(comments: List<ProfileCommentWidgetItem>) = ProfileCommentsWidgetView(
        profileId = UUID.randomUUID(),
        nickname = "Кіріто",
        page = 0,
        totalPages = 1,
        totalItems = comments.size.toLong(),
        rootCommentId = null,
        comments = comments,
    )

    private fun comment(content: String, author: String = "Asuna") = ProfileCommentWidgetItem(
        UUID.randomUUID(), author, content, false, 10, 3, "2026-11-10 15:00",
    )
}
