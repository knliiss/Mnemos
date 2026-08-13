package com.mnemos.notification.adapter.out.persistence.moderation

import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class NotificationSuspensionRepository(
    private val repository: NotificationSuspensionJpaRepository,
) {
    fun activate(
        sanctionId: UUID,
        userId: UUID,
        occurredAt: Instant,
    ) {
        val existing = repository.findById(sanctionId).orElse(null)

        if (existing != null && existing.occurredAt.isAfter(occurredAt)) {
            return
        }

        repository.save(
            NotificationSuspensionEntity(
                sanctionId = sanctionId,
                userId = userId,
                active = true,
                occurredAt = occurredAt,
            ),
        )
    }

    fun deactivate(
        sanctionId: UUID,
        userId: UUID,
        occurredAt: Instant,
    ) {
        val existing = repository.findById(sanctionId).orElse(null)

        if (existing != null && existing.occurredAt.isAfter(occurredAt)) {
            return
        }

        repository.save(
            NotificationSuspensionEntity(
                sanctionId = sanctionId,
                userId = userId,
                active = false,
                occurredAt = occurredAt,
            ),
        )
    }

    fun isSuspended(userId: UUID): Boolean = repository.findAllByUserIdAndActiveTrue(userId).isNotEmpty()
}
