package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.daily.DailyWidgetView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class DailyWidgetRendererTest {
    private val renderer = DailyWidgetRenderer(ObjectMapper())
    private val json = ObjectMapper()

    @Test
    fun dailyRewardActionsRemainInSelfContainedWidget() {
        val view =
            DailyWidgetView(
                accountId = UUID.randomUUID(),
                streak = 7,
                claimedToday = false,
                recoverable = true,
                recoverableStreak = 6,
                restoresRemaining = 1,
                nextCoinMin = 20,
                nextCoinMax = 80,
                primeChance = 0.1,
                rewardDate = "2026-11-09",
            )

        val widget = renderer.render(view)
        val state = json.readTree(widget.chatData)

        assertTrue(widget.allowQueries)
        assertTrue(widget.html.contains("w1:daily:restore"))
        assertTrue(widget.html.contains("w1:daily:claim"))
        assertEquals(7, state.path("streak").asInt())
        assertEquals(1, state.path("restores").asInt())
        assertTrue(widget.chatData!!.toByteArray(Charsets.UTF_8).size <= 1024)
    }
}
