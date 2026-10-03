package com.mnemos.notification.application.ping

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.recipient.TelegramUserIdentityService
import org.springframework.stereotype.Service

@Service
class TelegramPingNotificationPreferenceService(
    private val userIdentityService: TelegramUserIdentityService,
    private val preferenceService: PingNotificationPreferenceService,
) {
    fun get(telegramId: TelegramId): Boolean {
        val userId = userIdentityService.resolve(telegramId) ?: return true
        return preferenceService.isEnabled(userId)
    }

    fun update(
        telegramId: TelegramId,
        enabled: Boolean,
    ): Boolean {
        val userId = userIdentityService.resolve(telegramId) ?: return true
        preferenceService.update(userId, enabled)
        return enabled
    }
}
