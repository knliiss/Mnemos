package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import org.springframework.stereotype.Component

@Component
class LocalTelegramMenuMessageRenderer : TelegramMenuMessageRenderer {
    override fun render(screen: TelegramMenuScreen): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderText(screen),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderActions(screen),
        )

    private fun renderText(screen: TelegramMenuScreen): String =
        when (screen) {
            TelegramMenuScreen.HOME ->
                """
                🧠 <b>Mnemos</b>

                Основные функции Mnemos доступны прямо в Telegram. Выберите раздел ниже.
                """.trimIndent()

            TelegramMenuScreen.NOTIFICATIONS ->
                """
                🔔 <b>Уведомления</b>

                Подписки на глобальные события, бустеры, данжи, рейды, дропы предметов и игроков.
                """.trimIndent()

            TelegramMenuScreen.ONLINE ->
                """
                🌐 <b>Онлайн SAO</b>

                Онлайн по реалмам и поиск текущего статуса игрока.
                """.trimIndent()

            TelegramMenuScreen.EVENTS ->
                """
                🕘 <b>История событий</b>

                Лента событий SAO с поиском, фильтрами и переходом к подпискам.
                """.trimIndent()

            TelegramMenuScreen.DAILY ->
                """
                🎁 <b>Daily</b>

                Ежедневная награда и текущая серия активного аккаунта.
                """.trimIndent()

            TelegramMenuScreen.ACCOUNT ->
                """
                👤 <b>Аккаунт</b>

                Активный Cristalix-аккаунт, баланс и переключение между аккаунтами.
                """.trimIndent()

            TelegramMenuScreen.SETTINGS ->
                """
                ⚙️ <b>Настройки</b>

                Язык, настройки Telegram-клиента и активный пак уведомлений.
                """.trimIndent()
        }

    private fun renderActions(screen: TelegramMenuScreen): List<OutboundTelegramMessageAction> =
        if (screen == TelegramMenuScreen.HOME) {
            homeActions
        } else {
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "← Назад",
                    TelegramMenuScreen.BACK_CALLBACK_DATA,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⌂ Меню",
                    TelegramMenuScreen.HOME.callbackData,
                ),
                OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
            )
        }

    private companion object {
        val homeActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "🔔 Уведомления",
                    TelegramMenuScreen.NOTIFICATIONS.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🌐 Онлайн",
                    TelegramMenuScreen.ONLINE.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🕘 История",
                    TelegramMenuScreen.EVENTS.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🎁 Daily",
                    TelegramMenuScreen.DAILY.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "👤 Аккаунт",
                    TelegramMenuScreen.ACCOUNT.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⚙️ Настройки",
                    TelegramMenuScreen.SETTINGS.callbackData,
                ),
                OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
            )
    }
}
