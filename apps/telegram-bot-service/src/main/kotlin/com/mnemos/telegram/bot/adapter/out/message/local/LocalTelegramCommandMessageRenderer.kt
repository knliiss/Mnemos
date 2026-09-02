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
        TelegramMessageLayout.normalize(
            OutboundTelegramMessage(
                text =
                    listOf(
                        "🤖 <b>Mnemos</b>",
                        TelegramMessageLayout.DIVIDER,
                        "",
                        "👋 <b>Добро пожаловать в Mnemos</b>",
                        "",
                        "Основными функциями Mnemos можно пользоваться прямо в этом чате — Mini App для этого не нужен.",
                        "",
                        "📋 <b>Откройте панель управления:</b>",
                    ).joinToString("\n"),
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actionRows = startActionRows,
            ).localized(language),
        )

    override fun renderHelp(
        section: TelegramHelpSection,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage {
        val rows = renderHelpRows(section)

        return TelegramMessageLayout.normalize(
            OutboundTelegramMessage(
                text = renderHelpText(section),
                parseMode = OutboundTelegramMessageParseMode.HTML,
                actions = rows.flatten(),
                actionRows = rows,
            ).localized(language),
        )
    }

    private fun renderHelpText(section: TelegramHelpSection): String =
        when (section) {
            TelegramHelpSection.OVERVIEW ->
                listOf(
                    "📋 <b>Команды Mnemos</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "/menu — главное меню",
                    "/account — аккаунты",
                    "/notifications — уведомления",
                    "/packs — паки уведомлений",
                    "/online — онлайн и реалмы",
                    "/events — история событий",
                    "/bets — ставки",
                    "/daily — Daily",
                    "/prime — PRIME",
                    "/settings — настройки",
                    "/help или /commands — этот список",
                    "",
                    "Для /online можно передать номер реалма или ник игрока. Для /events — строку поиска.",
                ).joinToString("\n")

            TelegramHelpSection.MINI_APP ->
                listOf(
                    "📱 <b>Mini App</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "Mini App — дополнительный интерфейс Mnemos. Если он открывается, используйте кнопку «Mini App» или стандартную кнопку Telegram.",
                    "",
                    "Если Mini App не загружается, основные функции остаются доступны через /menu.",
                ).joinToString("\n")

            TelegramHelpSection.SUBSCRIPTIONS ->
                listOf(
                    "🔔 <b>Подписки</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "🌌 Глобальные события",
                    "⚡ Бустеры",
                    "🏰 Dungeon",
                    "🛡 RAID",
                    "📦 Дропы",
                    "",
                    "Для фильтров по игроку или предмету бот сам запросит значение обычным сообщением.",
                ).joinToString("\n")

            TelegramHelpSection.ACCOUNTS ->
                listOf(
                    "👤 <b>Аккаунты</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "Откройте /account. Там можно создать Cristalix-аккаунт, присоединиться по invite-коду, посмотреть доступные аккаунты и переключить активный.",
                    "",
                    "COIN относится к активному Cristalix-аккаунту. PRIME относится к вашему пользователю Mnemos и не переключается вместе с аккаунтом.",
                ).joinToString("\n")

            TelegramHelpSection.BETS ->
                listOf(
                    "🎲 <b>Ставки</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "Откройте /bets. Для каждого типа события показывается только ближайший раунд.",
                    "",
                    "Нажмите «Да» или «Нет», затем отправьте сумму COIN обычным сообщением. Уже созданную ставку можно увеличить.",
                ).joinToString("\n")

            TelegramHelpSection.NOTIFICATIONS ->
                listOf(
                    "✉️ <b>Уведомления</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "Если бот был заблокирован, разблокируйте его и отправьте /start или /help — Mnemos снова активирует Telegram-получателя.",
                    "",
                    "Паки уведомлений доступны через /packs или кнопку «Паки уведомлений» внутри /notifications.",
                ).joinToString("\n")

            TelegramHelpSection.TROUBLESHOOTING ->
                listOf(
                    "🛠 <b>Проблемы и решения</b>",
                    TelegramMessageLayout.DIVIDER,
                    "",
                    "<b>Mini App не открывается.</b>",
                    "Используйте /menu — основные функции доступны в боте.",
                    "",
                    "<b>Раздел временно недоступен.</b>",
                    "Нажмите «Обновить». Бот покажет локальную ошибку, если backend-сервис не отвечает.",
                    "",
                    "<b>Нужен активный Cristalix-аккаунт.</b>",
                    "Перейдите в /account и создайте, подключите или переключите его.",
                ).joinToString("\n")
        }

    private fun renderHelpRows(section: TelegramHelpSection): List<List<OutboundTelegramMessageAction>> =
        buildList {
            add(listOf(menuAction))

            helpSectionGroups.forEach { group ->
                add(
                    group.map { (helpSection, label) ->
                        OutboundTelegramMessageAction.Callback(
                            label = label,
                            data = helpSection.callbackData,
                            style = selectedStyle(helpSection == section),
                        )
                    },
                )
            }

            add(listOf(OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App")))
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
        val menuAction =
            OutboundTelegramMessageAction.Callback(
                label = "📋 Меню Mnemos",
                data = TelegramMenuScreen.OPEN_CALLBACK_DATA,
                style = OutboundTelegramButtonStyle.PRIMARY,
            )

        val helpAction =
            OutboundTelegramMessageAction.Callback(
                label = "❓ Помощь",
                data = TelegramHelpSection.OVERVIEW.callbackData,
            )

        val startActionRows =
            listOf(
                listOf(menuAction),
                listOf(helpAction),
                listOf(
                    OutboundTelegramMessageAction.OpenMiniApp("↗️ Mini App"),
                ),
            )

        val helpSectionGroups =
            listOf(
                listOf(
                    TelegramHelpSection.OVERVIEW to "Команды",
                    TelegramHelpSection.MINI_APP to "Mini App",
                ),
                listOf(
                    TelegramHelpSection.SUBSCRIPTIONS to "Подписки",
                    TelegramHelpSection.ACCOUNTS to "Аккаунты",
                    TelegramHelpSection.BETS to "Ставки",
                ),
                listOf(
                    TelegramHelpSection.NOTIFICATIONS to "Уведомления",
                    TelegramHelpSection.TROUBLESHOOTING to "Проблемы",
                ),
            )
    }
}
