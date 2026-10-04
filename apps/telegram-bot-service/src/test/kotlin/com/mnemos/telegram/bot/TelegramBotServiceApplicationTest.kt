package com.mnemos.telegram.bot

import com.pengrad.telegrambot.TelegramBot
import feign.Client
import feign.hc5.ApacheHttp5Client
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@Tag("integration")
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

    @Test
    fun `feign uses Apache HTTP client with PATCH support`() {
        val feignClient = applicationContext.getBean(Client::class.java)

        assertInstanceOf(ApacheHttp5Client::class.java, feignClient)
    }
}
