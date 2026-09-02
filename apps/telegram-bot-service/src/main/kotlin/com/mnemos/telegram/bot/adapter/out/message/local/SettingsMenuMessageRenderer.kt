package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.settings.SettingsCallbackData
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult

object SettingsMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val settingsData = data as? TelegramMenuScreenData.Settings

        if (settingsData?.snapshot == null) {
            return unavailableMessage()
        }

        val rows = renderRows(settingsData)

        return OutboundTelegramMessage(
            text = renderText(settingsData),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun renderText(data: TelegramMenuScreenData.Settings): String {
        val snapshot = requireNotNull(data.snapshot)
        val language = snapshot.localizationLanguage?.displayName ?: "Недоступен"
        val packTitle =
            snapshot.activeNotificationPackTitle
                ?.takeIf(String::isNotBlank)
                ?.let(::escapeHtml)
                ?: "Не выбран"
        val lines =
            mutableListOf(
                "⚙️ <b>Настройки</b>",
                MESSAGE_DIVIDER,
                "",
                "🌐 <b>Язык</b>",
                "└ ${escapeHtml(language)}",
                "",
                "🔔 <b>Пак уведомлений</b>",
                "└ $packTitle",
            )

        when (data.mutationResult) {
            TelegramSettingsMutationResult.UPDATED -> {
                lines += ""
                lines += "✅ Настройка сохранена."
            }

            TelegramSettingsMutationResult.UNAVAILABLE -> {
                lines += ""
                lines += "⚠️ Не удалось сохранить настройку. Попробуйте ещё раз."
            }

            null -> Unit
        }

        if (snapshot.notificationPacks.isEmpty()) {
            lines += ""
            lines += "Паки уведомлений пока недоступны."
        }

        return lines.joinToString("\n")
    }

    private fun renderRows(data: TelegramMenuScreenData.Settings): List<List<OutboundTelegramMessageAction>> {
        val snapshot = requireNotNull(data.snapshot)
        val languageRow =
            TelegramSettingsLanguage.entries.map { language ->
                OutboundTelegramMessageAction.Callback(
                    label = language.shortLabel,
                    data = SettingsCallbackData.language(language),
                    style = selectedStyle(snapshot.localizationLanguage == language),
                )
            }

        return buildList {
            add(languageRow)

            snapshot.notificationPacks.forEach { pack ->
                add(
                    listOf(
                        OutboundTelegramMessageAction.Callback(
                            label = packButtonLabel(pack.title),
                            data = SettingsCallbackData.notificationPack(pack.personalizationId),
                            style = selectedStyle(pack.active),
                        ),
                    ),
                )
            }

            add(listOf(refreshAction))
            add(listOf(backAction))
        }
    }

    private fun unavailableMessage(): OutboundTelegramMessage {
        val rows =
            listOf(
                listOf(refreshAction),
                listOf(backAction),
            )

        return OutboundTelegramMessage(
            text =
                listOf(
                    "⚙️ <b>Настройки</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "⚠️ Не удалось загрузить персональные настройки.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun packButtonLabel(title: String): String = "🔔 " + title.trim().ifBlank { "Без названия" }.take(MAX_PACK_BUTTON_TITLE_LENGTH)

    private fun selectedStyle(selected: Boolean): OutboundTelegramButtonStyle =
        if (selected) {
            OutboundTelegramButtonStyle.SUCCESS
        } else {
            OutboundTelegramButtonStyle.DEFAULT
        }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private val TelegramSettingsLanguage.displayName: String
        get() =
            when (this) {
                TelegramSettingsLanguage.UK -> "🇺🇦 Українська"
                TelegramSettingsLanguage.RU -> "🇷🇺 Русский"
                TelegramSettingsLanguage.EN -> "🇬🇧 English"
            }

    private val TelegramSettingsLanguage.shortLabel: String
        get() =
            when (this) {
                TelegramSettingsLanguage.UK -> "🇺🇦 UA"
                TelegramSettingsLanguage.RU -> "🇷🇺 RU"
                TelegramSettingsLanguage.EN -> "🇬🇧 EN"
            }

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"
    private const val MAX_PACK_BUTTON_TITLE_LENGTH = 28

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔄 Обновить",
            data = SettingsCallbackData.REFRESH,
        )

    private val backAction =
        OutboundTelegramMessageAction.Callback(
            label = "← Назад",
            data = TelegramMenuScreen.BACK_CALLBACK_DATA,
        )
}
