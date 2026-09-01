package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.springframework.stereotype.Component

@Component
class LocalTelegramCommandMessageRenderer : TelegramCommandMessageRenderer {
    override fun renderStart(
        context: TelegramStartContext,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                "👋 <b>Добро пожаловать в Mnemos</b>\n\n" +
                    "Основными функциями Mnemos можно пользоваться прямо в этом чате — Mini App для этого не нужен.\n\n" +
                    "• создавайте и подключайте Cristalix-аккаунты;\n" +
                    "• настраивайте уведомления и смотрите онлайн/историю событий;\n" +
                    "• забирайте Daily и управляйте балансами;\n" +
                    "• делайте ставки и покупайте PRIME через Telegram Stars;\n" +
                    "• меняйте доступные настройки Mnemos.\n\n" +
                    "Нажмите «Меню Mnemos» или используйте /menu. Mini App ниже остаётся дополнительным интерфейсом, если он доступен в вашем регионе.",
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actionRows =
                listOf(
                    listOf(
                        OutboundTelegramMessageAction.Callback(
                            label = "🧠 Меню Mnemos",
                            data = TelegramMenuScreen.OPEN_CALLBACK_DATA,
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    ),
                    listOf(
                        OutboundTelegramMessageAction.Callback(
                            label = "❓ Помощь",
                            data = TelegramHelpSection.OVERVIEW.callbackData,
                        ),
                        OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App"),
                    ),
                ),
        ).localized(language)

    override fun renderHelp(
        section: TelegramHelpSection,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderHelpText(section),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderHelpActions(section),
        ).localized(language)

    private fun renderHelpText(section: TelegramHelpSection): String =
        when (section) {
            TelegramHelpSection.OVERVIEW ->
                """
                ❓ <b>Помощь Mnemos</b>

                <b>С чего начать?</b>
                Откройте /menu. Основные функции Mnemos работают прямо в Telegram-чате, включая настройку Cristalix-аккаунта.

                <b>Основные команды</b>
                /notifications · /online · /events · /daily · /account · /bets · /prime · /settings

                <b>Нужен ли Mini App?</b>
                Нет для основных сценариев. Он остаётся дополнительным визуальным интерфейсом и не должен блокировать использование Mnemos, если недоступен в вашем регионе.
                """.trimIndent()

            TelegramHelpSection.MINI_APP ->
                """
                📱 <b>Mini App</b>

                Mini App — дополнительный интерфейс Mnemos. Если он открывается, используйте кнопку «Mini App» или стандартную кнопку Telegram.

                Если Mini App не загружается из-за сети или региональных ограничений, продолжайте пользоваться /menu: аккаунт, уведомления, Daily, события, ставки, PRIME и настройки доступны через чат.
                """.trimIndent()

            TelegramHelpSection.SUBSCRIPTIONS ->
                """
                🔔 <b>Подписки</b>

                Откройте /notifications. Управление подписками повторяет Mini App: глобальные события, бустеры и данжи переключаются как настройки, RAID редактируется как одна подписка, а правила дропа управляются отдельно.

                Для фильтров по игроку или предмету бот сам запросит значение обычным сообщением.
                """.trimIndent()

            TelegramHelpSection.ACCOUNTS ->
                """
                👤 <b>Аккаунты</b>

                Откройте /account. Там можно создать Mnemos/Cristalix-аккаунт, присоединиться по invite-коду, посмотреть доступные аккаунты и переключить активный.

                Daily, ставки и другие account-scoped функции используют выбранный активный аккаунт и его права.
                """.trimIndent()

            TelegramHelpSection.BETS ->
                """
                🎲 <b>Ставки</b>

                Откройте /bets. Бот показывает текущие раунды, коэффициенты и вашу ставку.

                Выберите «Да» или «Нет», отправьте сумму COIN обычным сообщением. Уже созданную ставку можно увеличить. История доступна в этом же разделе.

                Для операции нужен активный аккаунт и право PLACE_BETS. Все ставки проходят через обычный Mnemos wallet/idempotency flow.
                """.trimIndent()

            TelegramHelpSection.NOTIFICATIONS ->
                """
                ✉️ <b>Уведомления</b>

                Если бот был заблокирован, разблокируйте его и отправьте /start или /help — Mnemos снова активирует Telegram-получателя.

                Notification pack и доступные пользовательские настройки можно менять через /settings.
                """.trimIndent()

            TelegramHelpSection.TROUBLESHOOTING ->
                """
                🛠 <b>Проблемы и решения</b>

                <b>Mini App не открывается или не загружается.</b>
                Используйте /menu. Для основных функций это не является блокирующей ошибкой.

                <b>Отдельный раздел бота временно недоступен.</b>
                Нажмите «Обновить». Если соответствующий backend-сервис не отвечает, бот покажет локальную ошибку без требования открыть Mini App.

                <b>Нужен активный Cristalix-аккаунт.</b>
                Перейдите в /account и создайте, подключите или переключите его прямо в Telegram.
                """.trimIndent()
        }

    private fun renderHelpActions(section: TelegramHelpSection): List<OutboundTelegramMessageAction> =
        buildList {
            add(
                OutboundTelegramMessageAction.Callback(
                    label = "🧠 Меню Mnemos",
                    data = TelegramMenuScreen.OPEN_CALLBACK_DATA,
                    style = OutboundTelegramButtonStyle.PRIMARY,
                ),
            )

            helpSections.forEach { (helpSection, label) ->
                add(
                    OutboundTelegramMessageAction.Callback(
                        label = if (helpSection == section) "✓ $label" else label,
                        data = helpSection.callbackData,
                        style =
                            if (helpSection == section) {
                                OutboundTelegramButtonStyle.SUCCESS
                            } else {
                                OutboundTelegramButtonStyle.DEFAULT
                            },
                    ),
                )
            }

            add(OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App"))
        }

    private fun OutboundTelegramMessage.localized(language: TelegramSettingsLanguage): OutboundTelegramMessage {
        if (language == TelegramSettingsLanguage.RU) {
            return this
        }

        return copy(
            text = BotCommandTranslations.translate(text, language),
            actions = actions.map { action -> action.localized(language) },
            actionRows =
                actionRows.map { row ->
                    row.map { action -> action.localized(language) }
                },
        )
    }

    private fun OutboundTelegramMessageAction.localized(language: TelegramSettingsLanguage): OutboundTelegramMessageAction =
        when (this) {
            is OutboundTelegramMessageAction.Callback ->
                copy(label = BotCommandTranslations.translate(label, language))

            is OutboundTelegramMessageAction.OpenMiniApp ->
                copy(label = BotCommandTranslations.translate(label, language))

            is OutboundTelegramMessageAction.OpenUrl ->
                copy(label = BotCommandTranslations.translate(label, language))
        }

    private companion object {
        val helpSections =
            listOf(
                TelegramHelpSection.OVERVIEW to "Обзор",
                TelegramHelpSection.MINI_APP to "Mini App",
                TelegramHelpSection.SUBSCRIPTIONS to "Подписки",
                TelegramHelpSection.ACCOUNTS to "Аккаунты",
                TelegramHelpSection.BETS to "Ставки",
                TelegramHelpSection.NOTIFICATIONS to "Уведомления",
                TelegramHelpSection.TROUBLESHOOTING to "Проблемы",
            )
    }
}
