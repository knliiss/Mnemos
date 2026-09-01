package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NotificationMenuGuidanceTest {
    @Test
    fun `drop creation explains how combined filters work`() {
        val message =
            NotificationMenuMessageRenderer.render(
                TelegramMenuScreenData.Notifications(
                    NotificationMenuScreenData(
                        result = NotificationSubscriptionResult.NotLoaded,
                        mode = NotificationCreationMode.ITEM_DROP,
                    ),
                ),
            )

        assertTrue(message.text.contains("совпадают все указанные фильтры"))
        assertTrue(message.text.contains("Только игрок — любой его дроп"))
        assertTrue(message.text.contains("Только предмет — этот предмет у любого игрока"))
    }

    @Test
    fun `raid editor explains immediate location updates`() {
        val message =
            NotificationMenuMessageRenderer.render(
                TelegramMenuScreenData.Notifications(
                    NotificationMenuScreenData(
                        result = NotificationSubscriptionResult.Available(page = emptyPage()),
                        mode = NotificationCreationMode.RAID,
                    ),
                ),
            )

        assertTrue(message.text.contains("Изменения применяются сразу"))
        assertTrue(message.text.contains("Нажмите номер локации"))
    }

    @Test
    fun `new guidance is localized`() {
        val source =
            "Правило срабатывает, когда совпадают все указанные фильтры. " +
                "Изменения применяются сразу. Все"

        val ukrainian = BotMenuDesignTranslations.translate(source, TelegramSettingsLanguage.UK)
        val english = BotMenuDesignTranslations.translate(source, TelegramSettingsLanguage.EN)

        assertTrue(ukrainian.contains("Правило спрацьовує"))
        assertTrue(ukrainian.contains("Зміни застосовуються одразу"))
        assertTrue(ukrainian.endsWith("Усі"))
        assertTrue(english.contains("The rule triggers"))
        assertTrue(english.contains("Changes apply immediately"))
        assertTrue(english.endsWith("All"))
    }

    private fun emptyPage(): NotificationSubscriptionPage =
        NotificationSubscriptionPage(
            items = emptyList(),
            page = 0,
            totalPages = 0,
            totalItems = 0,
            hasNextPage = false,
        )
}
