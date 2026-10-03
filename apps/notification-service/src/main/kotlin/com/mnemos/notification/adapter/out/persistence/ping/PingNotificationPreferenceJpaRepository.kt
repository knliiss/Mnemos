package com.mnemos.notification.adapter.out.persistence.ping

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PingNotificationPreferenceJpaRepository : JpaRepository<PingNotificationPreferenceEntity, UUID> {
    fun findAllByUserIdIn(userIds: Collection<UUID>): List<PingNotificationPreferenceEntity>
}
