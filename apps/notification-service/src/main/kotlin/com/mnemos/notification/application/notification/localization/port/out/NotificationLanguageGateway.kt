package com.mnemos.notification.application.notification.localization.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage

fun interface NotificationLanguageGateway {
    fun findLanguage(userId: UserId): LocalizationLanguage?
}
