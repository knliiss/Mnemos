package com.mnemos.profile.application.profile.usecase.projectaccount

import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.application.profile.support.PendingSocialSummaryCoordinator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ProjectAccountCreatedService(
    private val repository: PublicProfileRepository,
    private val pendingCoordinator: PendingSocialSummaryCoordinator,
) : ProjectAccountCreatedUseCase {
    @Transactional
    override fun execute(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        occurredAt: Instant,
    ) {
        repository.ensureExists(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            createdAt = occurredAt,
        )

        pendingCoordinator.apply(accountId)
    }
}
