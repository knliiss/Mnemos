package com.mnemos.telegram.bot.adapter.out.settings

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import org.springframework.stereotype.Component

@Component
class SettingsGatewayAdapter(
    private val client: PersonalizationServiceClient,
) : SettingsGateway {
    override fun getSettings(telegramId: Long): TelegramSettingsSnapshot? =
        try {
            val selection = client.getNotificationPackSelection(telegramId)

            TelegramSettingsSnapshot(
                activeNotificationPackTitle = selection?.pack?.mixedAggregateTitle,
            )
        } catch (_: RuntimeException) {
            null
        }
}
