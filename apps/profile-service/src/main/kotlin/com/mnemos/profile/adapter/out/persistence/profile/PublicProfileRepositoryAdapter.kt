package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.adapter.out.persistence.profile.PublicProfilePersistenceMapper.toDomain
import com.mnemos.profile.adapter.out.persistence.profile.PublicProfilePersistenceMapper.toEntity
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceRepository
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class PublicProfileRepositoryAdapter(
    private val repository: PublicProfileJpaRepository,
    private val appearanceRepository: ProfileAppearanceRepository,
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

    override fun save(profile: PublicProfile): PublicProfile {
        val saved = repository.save(profile.toEntity()).toDomain()

        return saved.withAppearance(appearanceRepository.findByAccountId(saved.accountId))
    }

    override fun findByAccountId(accountId: UUID): PublicProfile? {
        val profile = repository.findById(accountId).orElse(null)?.toDomain() ?: return null

        return profile.withAppearance(appearanceRepository.findByAccountId(accountId))
    }

    override fun findByAccountIdForUpdate(accountId: UUID): PublicProfile? {
        val profile = repository.findForUpdate(accountId)?.toDomain() ?: return null

        return profile.withAppearance(appearanceRepository.findByAccountId(accountId))
    }

    override fun search(
        query: String,
        pageable: Pageable,
    ): Page<PublicProfile> {
        val profiles = repository.searchByNickname(query, pageable)
        val appearances =
            appearanceRepository.findByAccountIds(
                profiles.content.map(PublicProfileEntity::accountId),
            )

        return profiles.map { entity ->
            entity
                .toDomain()
                .withAppearance(appearances[entity.accountId])
        }
    }

    private fun PublicProfile.withAppearance(appearance: ProfileAppearance?): PublicProfile =
        copy(
            appearance = appearance ?: ProfileAppearance.empty(accountId),
        )
}
