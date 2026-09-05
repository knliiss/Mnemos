package com.mnemos.notification.adapter.out.account

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.localization.port.out.NotificationLanguageGateway
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class AccountNotificationLanguageGatewayAdapter(
    private val client: NotificationAccountServiceClient,
) : NotificationLanguageGateway {
    override fun findLanguage(userId: UserId): LocalizationLanguage? =
        try {
            val response = client.getUserLocalization(userId.id)

            LocalizationLanguage.valueOf(response.localizationLanguage.name)
        } catch (_: FeignException.NotFound) {
            null
        }
}
