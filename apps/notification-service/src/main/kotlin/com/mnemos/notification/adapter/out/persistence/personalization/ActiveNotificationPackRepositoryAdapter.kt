package com.mnemos.notification.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.personalization.ActiveNotificationPackPersistenceMapper.toDomain
import com.mnemos.notification.adapter.out.persistence.personalization.ActiveNotificationPackPersistenceMapper.toEntity
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import org.springframework.stereotype.Repository
import java.time.Clock

@Repository
class ActiveNotificationPackRepositoryAdapter(
    private val repository: ActiveNotificationPackJpaRepository,
    private val clock: Clock,
) : ActiveNotificationPackRepository {
    override fun find(userId: UserId): ActiveNotificationPack? =
        repository.findById(userId.id).orElse(null)?.toDomain()

    override fun save(pack: ActiveNotificationPack): ActiveNotificationPack =
        repository.save(pack.toEntity(clock.instant())).toDomain()

    override fun delete(userId: UserId) {
        repository.deleteById(userId.id)
    }
}
