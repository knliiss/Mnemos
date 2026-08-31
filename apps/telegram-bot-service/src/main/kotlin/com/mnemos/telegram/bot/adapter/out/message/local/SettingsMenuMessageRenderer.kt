package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
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

        return OutboundTelegramMessage(
            text = renderText(settingsData),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderActions(settingsData),
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
                "",
                "Язык: <b>${escapeHtml(language)}</b>",
                "Пак уведомлений: <b>$packTitle</b>",
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
            lines += "Доступных паков уведомлений пока нет."
        }

        return lines.joinToString("\n")
    }

    private fun renderActions(data: TelegramMenuScreenData.Settings): List<OutboundTelegramMessageAction> {
        val snapshot = requireNotNull(data.snapshot)
        val languageActions =
            TelegramSettingsLanguage.entries.map { language ->
                OutboundTelegramMessageAction.Callback(
                    label = language.buttonLabel(snapshot.localizationLanguage == language),
                    data = SettingsCallbackData.language(language),
                )
            }
        val packActions =
            snapshot.notificationPacks.map { pack ->
                OutboundTelegramMessageAction.Callback(
                    label = packButtonLabel(pack.title, pack.active),
                    data = SettingsCallbackData.notificationPack(pack.personalizationId),
                )
            }

        return languageActions + packActions + refreshAndNavigationActions
    }

    private fun unavailableMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "⚙️ <b>Настройки</b>",
                    "",
                    "Не удалось загрузить персональные настройки. Попробуйте позже.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = refreshAndNavigationActions,
        )

    private fun TelegramSettingsLanguage.buttonLabel(selected: Boolean): String {
        val prefix = if (selected) "✓ " else ""

        return prefix + displayName
    }

    private fun packButtonLabel(
        title: String,
        selected: Boolean,
    ): String {
        val normalizedTitle = title.trim().ifBlank { "Без названия" }.take(MAX_PACK_BUTTON_TITLE_LENGTH)
        val prefix = if (selected) "✓ " else ""

        return prefix + normalizedTitle
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

    private val refreshAndNavigationActions =
        listOf(
            OutboundTelegramMessageAction.Callback(
                "🔄 Обновить",
                SettingsCallbackData.REFRESH,
            ),
            OutboundTelegramMessageAction.Callback(
                "← Назад",
                TelegramMenuScreen.BACK_CALLBACK_DATA,
            ),
            OutboundTelegramMessageAction.Callback(
                "⌂ Меню",
                TelegramMenuScreen.HOME.callbackData,
            ),
        )

    private const val MAX_PACK_BUTTON_TITLE_LENGTH = 28
}
