package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class AccountMenuTranslationsTest {
    @Test
    fun `account creation prompt is localized to Ukrainian`() {
        val translated =
            AccountMenuTranslations.translate(
                value = "✏️ Отправьте <b>ник Cristalix</b> обычным сообщением.",
                language = TelegramSettingsLanguage.UK,
            )

        assertEquals(
            "✏️ Надішліть <b>нік Cristalix</b> звичайним повідомленням.",
            translated,
        )
    }

    @Test
    fun `identity conflict explains why account cannot be created in English`() {
        val translated =
            AccountMenuTranslations.translate(
                value =
                    "⚠️ Этот Cristalix-аккаунт уже зарегистрирован в Mnemos и не может быть создан повторно. " +
                        "Если это ваш аккаунт, присоединитесь к нему по приглашению.",
                language = TelegramSettingsLanguage.EN,
            )

        assertFalse(translated.contains("зарегистрирован"))
        assertEquals(
            "⚠️ This Cristalix account is already registered in Mnemos and cannot be created again. " +
                "If it is your account, join it using an invite.",
            translated,
        )
    }

    @Test
    fun `Russian account feedback remains unchanged`() {
        val value = "Игрок Cristalix с таким ником не найден. Проверьте написание и отправьте ник ещё раз."

        val translated =
            AccountMenuTranslations.translate(
                value = value,
                language = TelegramSettingsLanguage.RU,
            )

        assertEquals(value, translated)
    }
}
