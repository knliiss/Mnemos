package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.adapter.out.persistence.profile.PublicProfilePersistenceMapper.toDomain
import com.mnemos.profile.adapter.out.persistence.profile.PublicProfilePersistenceMapper.toEntity
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class PublicProfileRepositoryAdapter(
    private val repository: PublicProfileJpaRepository,
) : PublicProfileRepository {
    override fun ensureExists(
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

    override fun save(profile: PublicProfile): PublicProfile =
        repository.save(profile.toEntity()).toDomain()

    override fun findByAccountId(accountId: UUID): PublicProfile? =
        repository.findById(accountId).orElse(null)?.toDomain()

    override fun findByAccountIdForUpdate(accountId: UUID): PublicProfile? =
        repository.findForUpdate(accountId)?.toDomain()

    override fun search(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile> =
        repository
            .searchByNickname(query, pageable)
            .map { entity -> entity.toDomain() }
}
