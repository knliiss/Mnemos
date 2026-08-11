package com.mnemos.notification.configuration.telegram

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TelegramBotPropertiesTest {
    @Test
    fun `disabled bot does not require credentials`() {
        assertDoesNotThrow {
            TelegramBotProperties()
        }
    }

    @Test
    fun `enabled bot requires token`() {
        assertThrows(IllegalArgumentException::class.java) {
            TelegramBotProperties(
                enabled = true,
                token = "",
                miniAppUrl = "https://app.example.com",
            )
        }
    }

    @Test
    fun `enabled bot requires https mini app url`() {
        assertThrows(IllegalArgumentException::class.java) {
            TelegramBotProperties(
                enabled = true,
                token = "token",
                miniAppUrl = "http://app.example.com",
            )
        }
    }

    @Test
    fun `enabled bot accepts token and https mini app url`() {
        assertDoesNotThrow {
            TelegramBotProperties(
                enabled = true,
                token = "token",
                miniAppUrl = "https://app.example.com",
            )
        }
    }
}
