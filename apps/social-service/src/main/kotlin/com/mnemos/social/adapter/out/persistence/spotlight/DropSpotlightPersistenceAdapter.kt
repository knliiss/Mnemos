package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.adapter.out.persistence.spotlight.DropSpotlightPersistenceMapper.toDomain
import com.mnemos.social.adapter.out.persistence.spotlight.DropSpotlightPersistenceMapper.toEntity
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class DropSpotlightPersistenceAdapter(
    private val repository: DropSpotlightJpaRepository,
) : DropSpotlightRepository {
    override fun save(spotlight: DropSpotlight): DropSpotlight = repository.save(spotlight.toEntity()).toDomain()

    override fun findById(id: DropSpotlightId): DropSpotlight? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdempotencyKey(idempotencyKey: UUID): DropSpotlight? = repository.findByIdempotencyKey(idempotencyKey)?.toDomain()

    override fun findByIdForUpdate(id: DropSpotlightId): DropSpotlight? = repository.findByIdForUpdate(id.id)?.toDomain()

    override fun existsNonFailedByItemDropEventId(itemDropEventId: UUID): Boolean =
        repository.existsByItemDropEventIdAndStatusNot(
            itemDropEventId = itemDropEventId,
            status = DropSpotlightStatus.PAYMENT_FAILED,
        )

    override fun findLatestByBuyerUserId(userId: UUID): DropSpotlight? =
        repository.findTopByBuyerUserIdOrderByCreatedAtDesc(userId)?.toDomain()

    override fun findNextDue(now: Instant): DropSpotlight? =
        repository
            .findFirstByStatusAndScheduledAtLessThanEqualOrderBySequenceAsc(
                status = DropSpotlightStatus.QUEUED,
                scheduledAt = now,
            )?.toDomain()

    override fun findLatestPublishedAt(): Instant? =
        repository
            .findTopByStatusAndPublishedAtIsNotNullOrderByPublishedAtDesc(DropSpotlightStatus.PUBLISHED)
            ?.publishedAt
}
