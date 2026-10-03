package com.mnemos.notification.application.ping.port.out

import com.mnemos.common.core.model.id.UserId

interface PingNotificationPreferenceRepository {
    fun findEnabled(userId: UserId): Boolean?

    fun findEnabled(userIds: Set<UserId>): Map<UserId, Boolean>

    fun save(
        userId: UserId,
        enabled: Boolean,
    )
}
