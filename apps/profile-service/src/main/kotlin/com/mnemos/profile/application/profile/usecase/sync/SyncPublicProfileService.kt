package com.mnemos.profile.application.profile.usecase.sync

import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.application.profile.support.PendingSocialSummaryCoordinator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SyncPublicProfileService(
    private val repository: PublicProfileRepository,
    private val pendingCoordinator: PendingSocialSummaryCoordinator,
) : SyncPublicProfileUseCase {
    @Transactional
    override fun execute(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        createdAt: Instant,
    ) {
        repository.ensureExists(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            createdAt = createdAt,
        )

        val current = requireNotNull(repository.findByAccountIdForUpdate(accountId))

        if (
            current.cristalixUuid != cristalixUuid ||
            current.cristalixNickname != cristalixNickname ||
            current.createdAt != createdAt
        ) {
            repository.save(
                current.copy(
                    cristalixUuid = cristalixUuid,
                    cristalixNickname = cristalixNickname,
                    createdAt = createdAt,
                ),
            )
        }

        pendingCoordinator.apply(accountId)
    }
}
