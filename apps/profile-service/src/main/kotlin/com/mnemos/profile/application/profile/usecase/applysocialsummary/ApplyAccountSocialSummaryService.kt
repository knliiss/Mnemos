package com.mnemos.profile.application.profile.usecase.applysocialsummary

import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.application.profile.support.PendingSocialSummaryCoordinator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ApplyAccountSocialSummaryService(
    private val repository: PublicProfileRepository,
    private val pendingCoordinator: PendingSocialSummaryCoordinator,
) : ApplyAccountSocialSummaryUseCase {
    @Transactional
    override fun execute(
        accountId: UUID,
        likes: Long,
        dislikes: Long,
        comments: Long,
        revision: Long,
        occurredAt: Instant,
    ) {
        val current = repository.findByAccountIdForUpdate(accountId)

        if (current == null) {
            pendingCoordinator.saveIfNewer(
                accountId = accountId,
                likes = likes,
                dislikes = dislikes,
                comments = comments,
                revision = revision,
                occurredAt = occurredAt,
            )

            return
        }

        if (revision > current.socialRevision) {
            repository.save(
                current.copy(
                    likes = likes,
                    dislikes = dislikes,
                    comments = comments,
                    socialRevision = revision,
                    updatedAt = maxOf(current.updatedAt, occurredAt),
                ),
            )
        }

        pendingCoordinator.apply(accountId)
    }
}
