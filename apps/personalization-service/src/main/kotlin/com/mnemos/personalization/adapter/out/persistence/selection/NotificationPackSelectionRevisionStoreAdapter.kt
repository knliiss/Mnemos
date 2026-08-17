package com.mnemos.personalization.adapter.out.persistence.selection

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.port.out.NotificationPackSelectionRevisionStore
import org.springframework.stereotype.Repository

@Repository
class NotificationPackSelectionRevisionStoreAdapter(
    private val repository: NotificationPackSelectionRevisionJpaRepository,
) : NotificationPackSelectionRevisionStore {
    override fun next(userId: UserId): Long {
        repository.ensureState(userId.id)

        val state =
            requireNotNull(repository.findByUserIdForUpdate(userId.id)) {
                "Notification pack selection revision state was not created"
            }

        return state.increment()
    }

    override fun current(userId: UserId): Long =
        repository
            .findById(userId.id)
            .map(NotificationPackSelectionRevisionEntity::revision)
            .orElse(0L)
}
