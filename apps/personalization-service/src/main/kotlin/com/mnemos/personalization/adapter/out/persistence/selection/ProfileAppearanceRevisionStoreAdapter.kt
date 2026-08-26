package com.mnemos.personalization.adapter.out.persistence.selection

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.application.ownership.event.port.out.ProfileAppearanceRevisionStore
import org.springframework.stereotype.Repository

@Repository
class ProfileAppearanceRevisionStoreAdapter(
    private val repository: ProfileAppearanceRevisionJpaRepository,
) : ProfileAppearanceRevisionStore {
    override fun next(accountId: AccountId): Long {
        repository.ensureState(accountId.id)

        val state =
            requireNotNull(repository.findByAccountIdForUpdate(accountId.id)) {
                "Profile appearance revision state was not created"
            }

        return state.increment()
    }

    override fun current(accountId: AccountId): Long = repository.findById(accountId.id).orElse(null)?.revision ?: 0L
}
