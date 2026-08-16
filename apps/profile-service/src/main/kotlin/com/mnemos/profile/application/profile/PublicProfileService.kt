package com.mnemos.profile.application.profile

import com.mnemos.profile.application.profile.port.out.PendingSocialSummaryRepository
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PendingSocialSummary
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class PublicProfileService(
    private val repository: PublicProfileRepository,
    private val pendingSocialSummaryRepository: PendingSocialSummaryRepository,
) {
    @Transactional(readOnly = true)
    fun get(accountId: UUID): PublicProfile =
        repository.findByAccountId(accountId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Public profile was not found")

    @Transactional(readOnly = true)
    fun search(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile> {
        val normalized = query.trim()

        if (normalized.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile search query cannot be blank")
        }

        return repository.search(normalized, pageable)
    }

    @Transactional
    fun accountCreated(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        occurredAt: Instant,
    ) {
        ensureProfileExists(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            createdAt = occurredAt,
        )

        applyPendingSocialSummary(accountId)
    }

    @Transactional
    fun bootstrapAccount(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        createdAt: Instant,
    ) {
        ensureProfileExists(
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

        applyPendingSocialSummary(accountId)
    }

    @Transactional
    fun socialSummaryChanged(
        accountId: UUID,
        likes: Long,
        dislikes: Long,
        comments: Long,
        revision: Long,
        occurredAt: Instant,
    ) {
        val current = repository.findByAccountIdForUpdate(accountId)

        if (current == null) {
            savePendingSocialSummary(
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

        applyPendingSocialSummary(accountId)
    }

    @Transactional
    fun reconcilePendingSocialSummaries(limit: Int) {
        require(limit > 0) {
            "Pending social summary reconciliation limit must be positive"
        }

        pendingSocialSummaryRepository
            .findReady(limit)
            .forEach { summary ->
                applyPendingSocialSummary(summary.accountId)
            }
    }

    private fun ensureProfileExists(
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
    }

    private fun savePendingSocialSummary(
        accountId: UUID,
        likes: Long,
        dislikes: Long,
        comments: Long,
        revision: Long,
        occurredAt: Instant,
    ) {
        val existing = pendingSocialSummaryRepository.findByAccountId(accountId)

        if (existing != null && revision <= existing.revision) {
            return
        }

        pendingSocialSummaryRepository.save(
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

    private fun applyPendingSocialSummary(accountId: UUID) {
        val profile = repository.findByAccountIdForUpdate(accountId) ?: return
        val pending = pendingSocialSummaryRepository.findByAccountId(accountId) ?: return

        if (pending.revision > profile.socialRevision) {
            repository.save(
                profile.copy(
                    likes = pending.likes,
                    dislikes = pending.dislikes,
                    comments = pending.comments,
                    socialRevision = pending.revision,
                    updatedAt = maxOf(profile.updatedAt, pending.occurredAt),
                ),
            )
        }

        pendingSocialSummaryRepository.deleteIfRevision(
            accountId = accountId,
            revision = pending.revision,
        )
    }
}
