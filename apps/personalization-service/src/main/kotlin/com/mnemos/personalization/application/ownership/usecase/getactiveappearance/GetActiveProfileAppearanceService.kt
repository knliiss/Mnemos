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
    override fun execute(accountId: AccountId): ActiveProfileAppearanceSnapshot =
        ActiveProfileAppearanceSnapshot(
            accountId = accountId,
            revision = revisionStore.current(accountId),
            appearance = snapshotResolver.resolve(accountId),
            observedAt = clock.instant(),
        )
}
