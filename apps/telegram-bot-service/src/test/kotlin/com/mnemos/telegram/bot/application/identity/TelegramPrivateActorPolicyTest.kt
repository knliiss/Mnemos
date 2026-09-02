package com.mnemos.telegram.bot.application.identity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TelegramPrivateActorPolicyTest {
    @Test
    fun `private actor is trusted when actor and chat ids match`() {
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = true,
                chatId = 931458258L,
                actorId = 931458258L,
            )

        assertEquals(931458258L, telegramId)
    }

    @Test
    fun `private actor is rejected when actor and chat ids differ`() {
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = true,
                chatId = 931458258L,
                actorId = 123456789L,
            )

        assertNull(telegramId)
    }

    @Test
    fun `private actor is rejected when actor is absent`() {
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = true,
                chatId = 931458258L,
                actorId = null,
            )

        assertNull(telegramId)
    }

    @Test
    fun `non private chat is never accepted as user identity`() {
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = false,
                chatId = 931458258L,
                actorId = 931458258L,
            )

        assertNull(telegramId)
    }
}
