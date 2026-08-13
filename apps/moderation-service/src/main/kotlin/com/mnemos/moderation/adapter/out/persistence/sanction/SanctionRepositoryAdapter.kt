package com.mnemos.moderation.adapter.out.persistence.sanction

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.adapter.out.persistence.sanction.SanctionPersistenceMapper.toDomain
import com.mnemos.moderation.adapter.out.persistence.sanction.SanctionPersistenceMapper.toEntity
import com.mnemos.moderation.adapter.out.persistence.sanction.SanctionPersistenceMapper.updateFrom
import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import com.mnemos.moderation.domain.sanction.model.Sanction
import com.mnemos.moderation.domain.sanction.model.SanctionId
import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class SanctionRepositoryAdapter(
    private val repository: SanctionJpaRepository,
) : SanctionRepository {
    override fun save(sanction: Sanction): Sanction {
        val existing = repository.findById(sanction.id.id).orElse(null)

        if (existing == null) {
            return repository.save(sanction.toEntity()).toDomain()
        }

        existing.updateFrom(sanction)
        return repository.save(existing).toDomain()
    }

    override fun findByIdForUpdate(id: SanctionId): Sanction? = repository.findByIdForUpdate(id.id)?.toDomain()

    override fun findActiveByUserIdAndTypeForUpdate(
        userId: UserId,
        type: DomainSanctionType,
    ): Sanction? =
        repository
            .findActiveByUserIdAndTypeForUpdate(
                userId = userId.id,
                type = type,
                status = DomainSanctionStatus.ACTIVE,
            )?.toDomain()

    override fun findExpired(
        now: Instant,
        limit: Int,
    ): List<Sanction> =
        repository
            .findExpired(
                status = DomainSanctionStatus.ACTIVE,
                now = now,
                pageable = PageRequest.of(0, limit),
            ).map { entity -> entity.toDomain() }

    override fun findActiveBans(now: Instant): List<Sanction> =
        repository
            .findActiveBans(
                type = DomainSanctionType.BAN,
                status = DomainSanctionStatus.ACTIVE,
                now = now,
            ).map { entity -> entity.toDomain() }

    override fun findByUserId(userId: UserId): List<Sanction> =
        repository
            .findAllByUserIdOrderByCreatedAtDesc(userId.id)
            .map { entity -> entity.toDomain() }
}
