package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Component

@Component
class LocalTelegramMenuMessageRenderer : TelegramMenuMessageRenderer {
    override fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderText(screen, data),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderActions(screen),
        )

    private fun renderText(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
    ): String =
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

            TelegramMenuScreen.ONLINE -> renderSaoOnline(data)

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

    private fun renderSaoOnline(data: TelegramMenuScreenData): String {
        val onlineData = data as? TelegramMenuScreenData.SaoOnline ?: return renderSaoOnlineUnavailable()

        return when (val result = onlineData.result) {
            is SaoOnlineSnapshotResult.Available -> renderSaoOnlineAvailable(result.snapshot)
            SaoOnlineSnapshotResult.Unavailable -> renderSaoOnlineUnavailable()
        }
    }

    private fun renderSaoOnlineAvailable(snapshot: SaoOnlineSnapshot): String {
        val realmLines =
            snapshot.realms.joinToString("\n") { realm ->
                "${realm.name} — <b>${realm.playerCount}</b>"
            }.ifBlank {
                "Нет активных реалмов."
            }
        val generatedAt = onlineTimestampFormatter.format(snapshot.generatedAt)

        return """
            🌐 <b>Онлайн SAO</b>

            Сейчас отслеживается: <b>${snapshot.trackedOnline}</b>

            $realmLines

            Обновлено: <code>$generatedAt</code>
        """.trimIndent()
    }

    private fun renderSaoOnlineUnavailable(): String =
        """
        🌐 <b>Онлайн SAO</b>

        Не удалось получить актуальный онлайн. Попробуйте обновить через несколько секунд.
        """.trimIndent()

    private fun renderActions(screen: TelegramMenuScreen): List<OutboundTelegramMessageAction> =
        when (screen) {
            TelegramMenuScreen.HOME -> homeActions
            TelegramMenuScreen.ONLINE -> onlineActions
            else -> navigationActions
        }

    private companion object {
        val navigationActions =
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

        val onlineActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "🔄 Обновить",
                    TelegramMenuScreen.ONLINE.callbackData,
                ),
            ) + navigationActions

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

        val onlineTimestampFormatter: DateTimeFormatter =
            DateTimeFormatter
                .ofPattern("dd.MM HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC)
    }
}
