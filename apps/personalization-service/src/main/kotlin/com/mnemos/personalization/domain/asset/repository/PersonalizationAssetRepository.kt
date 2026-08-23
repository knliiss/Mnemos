package com.mnemos.personalization.domain.asset.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId

interface PersonalizationAssetRepository {
    fun save(asset: PersonalizationAsset): PersonalizationAsset

    fun findById(id: PersonalizationAssetId): PersonalizationAsset?

    fun findByOwnerUserId(ownerUserId: UserId): List<PersonalizationAsset>

    fun existsByIdAndOwnerUserId(
        id: PersonalizationAssetId,
        ownerUserId: UserId,
    ): Boolean
}
