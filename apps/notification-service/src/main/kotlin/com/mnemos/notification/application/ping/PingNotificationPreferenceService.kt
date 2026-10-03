package com.mnemos.notification.application.ping

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.ping.port.out.PingNotificationPreferenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PingNotificationPreferenceService(
    private val repository: PingNotificationPreferenceRepository,
) {
    @Transactional(readOnly = true)
    fun isEnabled(userId: UserId): Boolean = repository.findEnabled(userId) ?: true

    @Transactional(readOnly = true)
    fun filterEnabled(userIds: Set<UserId>): Set<UserId> {
        if (userIds.isEmpty()) return emptySet()

        val stored = repository.findEnabled(userIds)
        return userIds.filterTo(linkedSetOf()) { userId -> stored[userId] ?: true }
    }

    @Transactional
    fun update(
        userId: UserId,
        enabled: Boolean,
    ) {
        repository.save(userId, enabled)
    }
}
