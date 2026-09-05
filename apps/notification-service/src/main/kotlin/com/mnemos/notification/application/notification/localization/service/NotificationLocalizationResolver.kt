package com.mnemos.notification.application.notification.localization.service

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.localization.model.NotificationLocalizationContext

fun interface NotificationLocalizationResolver {
    fun resolve(userId: UserId): NotificationLocalizationContext
}
