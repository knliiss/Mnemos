package com.mnemos.telegram.bot

import com.pengrad.telegrambot.TelegramBot
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest(
    properties = [
        "mnemos.telegram-bot.telegram.enabled=false",
    ],
)
class TelegramBotServiceApplicationTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `application context starts without telegram bot when disabled`() {
        val telegramBot = applicationContext.getBeanProvider(TelegramBot::class.java).ifAvailable

        assertNull(telegramBot)
    }
}
