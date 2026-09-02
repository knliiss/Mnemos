package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BotMenuDesignTranslationsTest {
    @Test
    fun `bet history statuses localize to ukrainian`() {
        val translated =
            BotMenuDesignTranslations.translate(
                value = "Статус: ожидает подтверждения · возврат обрабатывается · выигрыш",
                language = TelegramSettingsLanguage.UK,
            )

        assertEquals(
            "Статус: очікує підтвердження · повернення обробляється · виграш",
            translated,
        )
    }

    @Test
    fun `bet history statuses localize to english`() {
        val translated =
            BotMenuDesignTranslations.translate(
                value = "Статус: ожидает результата · выплата не выполнена · проигрыш",
                language = TelegramSettingsLanguage.EN,
            )

        assertEquals(
            "Status: awaiting result · payout failed · lost",
            translated,
        )
    }
}
