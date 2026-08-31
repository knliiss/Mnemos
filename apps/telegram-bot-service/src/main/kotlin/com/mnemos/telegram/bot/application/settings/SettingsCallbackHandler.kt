package com.mnemos.telegram.bot.application.settings

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class SettingsCallbackHandler(
    private val gateway: SettingsGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = SettingsCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (!isCurrentSettingsMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        if (context.data == SettingsCallbackData.REFRESH) {
            return update(context, null)
        }

        SettingsCallbackData.languageFrom(context.data)?.let { language ->
            val result = gateway.updateLanguage(context.telegramId, language)
            return update(context, result)
        }

        SettingsCallbackData.notificationPackIdFrom(context.data)?.let { personalizationId ->
            val result = gateway.activateNotificationPack(context.telegramId, personalizationId)
            return update(context, result)
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun update(
        context: TelegramCallbackContext,
        result: TelegramSettingsMutationResult?,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.SETTINGS,
            update = {
                if (result == null) {
                    emptyMap()
                } else {
                    mapOf(MUTATION_RESULT_CONTEXT_KEY to result.name)
                }
            },
        )

    private fun isCurrentSettingsMenu(context: TelegramCallbackContext): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.SETTINGS && session.menuMessageId == context.messageId
    }

    companion object {
        const val MUTATION_RESULT_CONTEXT_KEY = "settings.mutation-result"
    }
}
