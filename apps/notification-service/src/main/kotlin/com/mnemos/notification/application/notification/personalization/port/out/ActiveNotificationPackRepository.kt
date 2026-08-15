package com.mnemos.notification.application.notification.personalization.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack

interface ActiveNotificationPackRepository {
    fun find(userId: UserId): ActiveNotificationPack?

    fun save(pack: ActiveNotificationPack): ActiveNotificationPack

    fun delete(userId: UserId)
}
