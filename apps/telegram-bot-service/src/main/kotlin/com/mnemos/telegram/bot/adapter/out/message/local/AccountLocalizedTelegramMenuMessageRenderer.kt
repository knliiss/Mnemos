package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class AccountLocalizedTelegramMenuMessageRenderer(
    private val delegate: LocalTelegramMenuMessageRenderer,
) : TelegramMenuMessageRenderer {
    override fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage {
        val rendered = delegate.render(screen, data, language)

        if (screen != TelegramMenuScreen.ACCOUNT || language == TelegramSettingsLanguage.RU) {
            return rendered
        }

        return rendered.copy(
            text = AccountMenuTranslations.translate(rendered.text, language),
            actions = rendered.actions.map { action -> action.localized(language) },
            actionRows =
                rendered.actionRows.map { row ->
                    row.map { action -> action.localized(language) }
                },
        )
    }

    private fun OutboundTelegramMessageAction.localized(language: TelegramSettingsLanguage): OutboundTelegramMessageAction =
        when (this) {
            is OutboundTelegramMessageAction.Callback ->
                copy(label = AccountMenuTranslations.translate(label, language))

            is OutboundTelegramMessageAction.OpenMiniApp ->
                copy(label = AccountMenuTranslations.translate(label, language))

            is OutboundTelegramMessageAction.OpenUrl ->
                copy(label = AccountMenuTranslations.translate(label, language))
        }
}
