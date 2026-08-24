package com.mnemos.notification.adapter.out.telegram

import com.mnemos.notification.adapter.out.telegram.TelegramMessageMapper.toReplyMarkup
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class TelegramMessageMapperTest {
    @Test
    fun `mini app action uses inline web app button`() {
        val message =
            RenderedMessage(
                text = "open app",
                actions =
                    listOf(
                        MessageAction.OpenMiniApp(
                            "Open Mnemos",
                        ),
                    ),
            )

        val markup = message.toReplyMarkup(MINI_APP_URL)
        val inlineMarkup = assertInstanceOf(InlineKeyboardMarkup::class.java, markup)
        val button = inlineMarkup.inlineKeyboard().single().single()

        assertEquals("Open Mnemos", button.text)
        assertEquals(MINI_APP_URL, button.webApp?.url())
    }

    private companion object {
        const val MINI_APP_URL = "https://app.example.com"
    }
}
