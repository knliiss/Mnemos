package com.mnemos.profile.application.profile

import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
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
        require(normalized.isNotEmpty()) { "Profile search query cannot be blank" }
        return repository.search(normalized, pageable)
    }

    @Transactional
    fun accountCreated(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        occurredAt: Instant,
    ) {
        if (repository.findByAccountId(accountId) != null) {
            return
        }

        repository.save(
            PublicProfile(
                accountId = accountId,
                cristalixUuid = cristalixUuid,
                cristalixNickname = cristalixNickname,
                likes = 0,
                dislikes = 0,
                comments = 0,
                socialRevision = 0,
                createdAt = occurredAt,
                updatedAt = occurredAt,
            ),
        )
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
        val current = repository.findByAccountId(accountId) ?: return
        if (revision <= current.socialRevision) {
            return
        }

        repository.save(
            current.copy(
                likes = likes,
                dislikes = dislikes,
                comments = comments,
                socialRevision = revision,
                updatedAt = occurredAt,
            ),
        )
    }
}
