package com.mnemos.personalization.adapter.out.persistence.asset

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.asset.PersonalizationAssetPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.asset.PersonalizationAssetPersistenceMapper.toEntity
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import org.springframework.stereotype.Component

@Component
class PersonalizationAssetRepositoryAdapter(
    private val repository: PersonalizationAssetJpaRepository,
) : PersonalizationAssetRepository {
    override fun save(asset: PersonalizationAsset): PersonalizationAsset =
        repository.saveAndFlush(asset.toEntity()).toDomain()

    override fun findById(id: PersonalizationAssetId): PersonalizationAsset? =
        repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByOwnerUserId(ownerUserId: UserId): List<PersonalizationAsset> =
        repository
            .findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId.id)
            .map { entity -> entity.toDomain() }

    override fun existsByIdAndOwnerUserId(
        id: PersonalizationAssetId,
        ownerUserId: UserId,
    ): Boolean = repository.existsByIdAndOwnerUserId(id.id, ownerUserId.id)
}
