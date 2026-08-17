package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.adapter.out.persistence.profile.ProfileAppearancePersistenceMapper.toDomain
import com.mnemos.profile.adapter.out.persistence.profile.ProfileAppearancePersistenceMapper.toEntity
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceRepository
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProfileAppearanceRepositoryAdapter(
    private val repository: ProfileAppearanceJpaRepository,
) : ProfileAppearanceRepository {
    override fun save(appearance: ProfileAppearance): ProfileAppearance = repository.save(appearance.toEntity()).toDomain()

    override fun findByAccountId(accountId: UUID): ProfileAppearance? = repository.findById(accountId).orElse(null)?.toDomain()

    override fun findByAccountIdForUpdate(accountId: UUID): ProfileAppearance? = repository.findForUpdate(accountId)?.toDomain()

    override fun findByAccountIds(accountIds: Collection<UUID>): Map<UUID, ProfileAppearance> {
        if (accountIds.isEmpty()) {
            return emptyMap()
        }

        return repository
            .findAllByAccountIdIn(accountIds)
            .associate { entity -> entity.accountId to entity.toDomain() }
    }
}
