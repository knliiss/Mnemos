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
                listOf(
                    "🤖 <b>Mnemos</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "👋 <b>Добро пожаловать в Mnemos</b>",
                    "",
                    "Основными функциями Mnemos можно пользоваться прямо в этом чате — Mini App для этого не нужен.",
                    "",
                    "Нажмите «Меню Mnemos» или используйте /menu. Mini App ниже остаётся дополнительным интерфейсом, если он доступен в вашем регионе.",
                    "",
                    "📋 <b>Откройте панель управления:</b>",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actionRows = startActionRows,
        ).localized(language)

    override fun renderHelp(
        section: TelegramHelpSection,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage {
        val rows = renderHelpRows(section)

        return OutboundTelegramMessage(
            text = renderHelpText(section),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        ).localized(language)
    }

    private fun renderHelpText(section: TelegramHelpSection): String =
        when (section) {
            TelegramHelpSection.OVERVIEW ->
                listOf(
                    "❓ <b>Помощь Mnemos</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "📋 <b>С чего начать?</b>",
                    "Откройте /menu. Основные функции Mnemos работают прямо в Telegram-чате, включая настройку Cristalix-аккаунта.",
                    "",
                    "📱 <b>Нужен ли Mini App?</b>",
                    "Нет для основных сценариев. Он остаётся дополнительным визуальным интерфейсом и не должен блокировать использование Mnemos, если недоступен в вашем регионе.",
                ).joinToString("\n")

            TelegramHelpSection.MINI_APP ->
                listOf(
                    "📱 <b>Mini App</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Mini App — дополнительный интерфейс Mnemos. Если он открывается, используйте кнопку «Mini App» или стандартную кнопку Telegram.",
                    "",
                    "Если Mini App не загружается из-за сети или региональных ограничений, продолжайте пользоваться /menu: аккаунт, уведомления, Daily, события, ставки, PRIME и настройки доступны через чат.",
                ).joinToString("\n")

            TelegramHelpSection.SUBSCRIPTIONS ->
                listOf(
                    "🔔 <b>Подписки</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Откройте /notifications. Управление подписками повторяет Mini App: глобальные события, бустеры и данжи переключаются как настройки, RAID редактируется как одна подписка, а правила дропа управляются отдельно.",
                    "",
                    "Для фильтров по игроку или предмету бот сам запросит значение обычным сообщением.",
                ).joinToString("\n")

            TelegramHelpSection.ACCOUNTS ->
                listOf(
                    "👤 <b>Аккаунты</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Откройте /account. Там можно создать Mnemos/Cristalix-аккаунт, присоединиться по invite-коду, посмотреть доступные аккаунты и переключить активный.",
                    "",
                    "Daily, ставки и другие account-scoped функции используют выбранный активный аккаунт и его права.",
                ).joinToString("\n")

            TelegramHelpSection.BETS ->
                listOf(
                    "🎲 <b>Ставки</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Откройте /bets. Бот показывает текущие раунды, коэффициенты и вашу ставку.",
                    "",
                    "Выберите «Да» или «Нет», отправьте сумму COIN обычным сообщением. Уже созданную ставку можно увеличить. История доступна в этом же разделе.",
                    "",
                    "Для операции нужен активный аккаунт и право PLACE_BETS. Все ставки проходят через обычный Mnemos wallet/idempotency flow.",
                ).joinToString("\n")

            TelegramHelpSection.NOTIFICATIONS ->
                listOf(
                    "✉️ <b>Уведомления</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Если бот был заблокирован, разблокируйте его и отправьте /start или /help — Mnemos снова активирует Telegram-получателя.",
                    "",
                    "Notification pack и доступные пользовательские настройки можно менять через /settings.",
                ).joinToString("\n")

            TelegramHelpSection.TROUBLESHOOTING ->
                listOf(
                    "🛠 <b>Проблемы и решения</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "<b>Mini App не открывается или не загружается.</b>",
                    "Используйте /menu. Для основных функций это не является блокирующей ошибкой.",
                    "",
                    "<b>Отдельный раздел бота временно недоступен.</b>",
                    "Нажмите «Обновить». Если соответствующий backend-сервис не отвечает, бот покажет локальную ошибку без требования открыть Mini App.",
                    "",
                    "<b>Нужен активный Cristalix-аккаунт.</b>",
                    "Перейдите в /account и создайте, подключите или переключите его прямо в Telegram.",
                ).joinToString("\n")
        }

    private fun renderHelpRows(section: TelegramHelpSection): List<List<OutboundTelegramMessageAction>> {
        val sectionActions =
            helpSections.map { (helpSection, label) ->
                OutboundTelegramMessageAction.Callback(
                    label = label,
                    data = helpSection.callbackData,
                    style = selectedStyle(helpSection == section),
                )
            }

        return buildList {
            add(listOf(menuAction))
            addAll(sectionActions.chunked(3))
            add(listOf(OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App")))
        }
    }

    private fun selectedStyle(selected: Boolean): OutboundTelegramButtonStyle =
        if (selected) {
            OutboundTelegramButtonStyle.SUCCESS
        } else {
            OutboundTelegramButtonStyle.DEFAULT
        }

    private fun OutboundTelegramMessage.localized(language: TelegramSettingsLanguage): OutboundTelegramMessage {
        if (language == TelegramSettingsLanguage.RU) {
            return this
        }

        return copy(
            text = translate(text, language),
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
                copy(label = translate(label, language))

            is OutboundTelegramMessageAction.OpenMiniApp ->
                copy(label = translate(label, language))

            is OutboundTelegramMessageAction.OpenUrl ->
                copy(label = translate(label, language))
        }

    private fun translate(
        value: String,
        language: TelegramSettingsLanguage,
    ): String =
        BotMenuDesignTranslations.translate(
            value = BotCommandTranslations.translate(value, language),
            language = language,
        )

    private companion object {
        const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

        val menuAction =
            OutboundTelegramMessageAction.Callback(
                label = "📋 Меню Mnemos",
                data = TelegramMenuScreen.OPEN_CALLBACK_DATA,
                style = OutboundTelegramButtonStyle.PRIMARY,
            )

        val startActionRows =
            listOf(
                listOf(menuAction),
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "❓ Помощь",
                        data = TelegramHelpSection.OVERVIEW.callbackData,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App"),
                ),
            )

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
