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
                    "👋 Добро пожаловать.",
                    "",
                    "Все основные функции доступны через единое меню прямо в Telegram.",
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
                    "📋 <b>Основное управление</b>",
                    "└ <code>/menu</code> — единая панель Mnemos.",
                    "",
                    "Все основные разделы доступны из меню. Mini App остаётся дополнительным интерфейсом.",
                ).joinToString("\n")

            TelegramHelpSection.MINI_APP ->
                listOf(
                    "📱 <b>Mini App</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Mini App — дополнительный визуальный интерфейс Mnemos.",
                    "",
                    "Если он недоступен, используйте <code>/menu</code>: основные функции работают прямо в чате.",
                ).joinToString("\n")

            TelegramHelpSection.SUBSCRIPTIONS ->
                listOf(
                    "🔔 <b>Уведомления</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "🌌 Глобальные события, ⚡ бустеры и 🏰 Dungeon переключаются прямо в панели.",
                    "🛡 RAID редактируется как одна существующая настройка.",
                    "📦 Правила дропа управляются отдельным списком.",
                ).joinToString("\n")

            TelegramHelpSection.ACCOUNTS ->
                listOf(
                    "👤 <b>Аккаунт</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "В одном разделе находятся:",
                    "├ 🔑 активный Cristalix-аккаунт",
                    "├ 💰 COIN и PRIME",
                    "├ 🎁 Daily",
                    "└ 👥 создание, подключение и переключение аккаунтов",
                ).joinToString("\n")

            TelegramHelpSection.BETS ->
                listOf(
                    "🎲 <b>Ставки</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "🎯 События, 🏁 Item Race и 📜 история находятся в одном разделе.",
                    "",
                    "Выберите действие и отправьте сумму COIN, когда бот запросит её.",
                ).joinToString("\n")

            TelegramHelpSection.NOTIFICATIONS ->
                listOf(
                    "✉️ <b>Доставка уведомлений</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "Если бот был заблокирован, разблокируйте его и отправьте <code>/start</code> или <code>/menu</code>.",
                    "",
                    "Пак уведомлений и язык меняются в разделе ⚙️ Настройки.",
                ).joinToString("\n")

            TelegramHelpSection.TROUBLESHOOTING ->
                listOf(
                    "🛠 <b>Проблемы</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "📱 Mini App не открывается — используйте <code>/menu</code>.",
                    "🔄 Раздел не загрузился — нажмите «Обновить».",
                    "👤 Нужен аккаунт — откройте раздел «Аккаунт» в меню.",
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
                TelegramHelpSection.SUBSCRIPTIONS to "Уведомления",
                TelegramHelpSection.ACCOUNTS to "Аккаунт",
                TelegramHelpSection.BETS to "Ставки",
                TelegramHelpSection.NOTIFICATIONS to "Доставка",
                TelegramHelpSection.TROUBLESHOOTING to "Проблемы",
            )
    }
}
