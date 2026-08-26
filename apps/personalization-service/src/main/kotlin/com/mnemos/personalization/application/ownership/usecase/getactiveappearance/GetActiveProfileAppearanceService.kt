package com.mnemos.personalization.application.ownership.usecase.getactiveappearance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceSnapshotResolver
import com.mnemos.personalization.application.ownership.event.port.out.ProfileAppearanceRevisionStore
import com.mnemos.personalization.application.ownership.model.ActiveProfileAppearanceSnapshot
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class GetActiveProfileAppearanceService(
    private val snapshotResolver: ProfileAppearanceSnapshotResolver,
    private val revisionStore: ProfileAppearanceRevisionStore,
    private val clock: Clock,
) : GetActiveProfileAppearanceUseCase {
    @Transactional(readOnly = true)
    override fun execute(accountId: AccountId): ActiveProfileAppearanceSnapshot {
        repeat(MAX_STABLE_READ_ATTEMPTS) {
            val revisionBefore = revisionStore.current(accountId)
            val appearance = snapshotResolver.resolve(accountId)
            val revisionAfter = revisionStore.current(accountId)

            if (revisionBefore == revisionAfter) {
                return ActiveProfileAppearanceSnapshot(
                    accountId = accountId,
                    revision = revisionAfter,
                    appearance = appearance,
                    observedAt = clock.instant(),
                )
            }
        }

        error("Profile appearance changed repeatedly while reading account ${accountId.id}")
    }

    private companion object {
        const val MAX_STABLE_READ_ATTEMPTS = 3
    }
}
