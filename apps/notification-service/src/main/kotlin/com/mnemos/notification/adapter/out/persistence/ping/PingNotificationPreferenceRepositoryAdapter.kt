package com.mnemos.notification.adapter.out.persistence.ping

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.ping.port.out.PingNotificationPreferenceRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PingNotificationPreferenceRepositoryAdapter(
    private val repository: PingNotificationPreferenceJpaRepository,
) : PingNotificationPreferenceRepository {
    override fun findEnabled(userId: UserId): Boolean? = repository.findById(userId.id).orElse(null)?.enabled

    override fun findEnabled(userIds: Set<UserId>): Map<UserId, Boolean> =
        repository
            .findAllByUserIdIn(userIds.map(UserId::id))
            .associate { entity -> UserId(entity.userId) to entity.enabled }

    override fun save(
        userId: UserId,
        enabled: Boolean,
    ) {
        repository.save(
            PingNotificationPreferenceEntity(
                userId = userId.id,
                enabled = enabled,
                updatedAt = Instant.now(),
            ),
        )
    }
}
