package com.mnemos.notification.adapter.out.persistence.moderation

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationSuspensionJpaRepository : JpaRepository<NotificationSuspensionEntity, UUID> {
    fun findAllByUserIdAndActiveTrue(userId: UUID): List<NotificationSuspensionEntity>
}
