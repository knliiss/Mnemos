package com.mnemos.profile.application.profile.support

import com.mnemos.profile.application.profile.port.out.PendingSocialSummaryRepository
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PendingSocialSummary
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class PendingSocialSummaryCoordinator(
    private val profileRepository: PublicProfileRepository,
    private val pendingRepository: PendingSocialSummaryRepository,
) {
    fun saveIfNewer(
        accountId: UUID,
        likes: Long,
        dislikes: Long,
        comments: Long,
        revision: Long,
        occurredAt: Instant,
    ) {
        pendingRepository.saveIfNewer(
            PendingSocialSummary(
                accountId = accountId,
                likes = likes,
                dislikes = dislikes,
                comments = comments,
                revision = revision,
                occurredAt = occurredAt,
            ),
        )
    }

    fun apply(accountId: UUID) {
        val profile = profileRepository.findByAccountIdForUpdate(accountId) ?: return
        val pending = pendingRepository.findByAccountId(accountId) ?: return

        if (pending.revision > profile.socialRevision) {
            profileRepository.save(
                profile.copy(
                    likes = pending.likes,
                    dislikes = pending.dislikes,
                    comments = pending.comments,
                    socialRevision = pending.revision,
                    updatedAt = maxOf(profile.updatedAt, pending.occurredAt),
                ),
            )
        }

        pendingRepository.deleteIfRevision(
            accountId = accountId,
            revision = pending.revision,
        )
    }
}
