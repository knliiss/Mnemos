package com.mnemos.personalization.application.asset.usecase

import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage
import com.mnemos.personalization.application.asset.port.out.PersonalizationAssetStorage
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import org.springframework.stereotype.Service

@Service
class GetPersonalizationAssetService(
    private val repository: PersonalizationAssetRepository,
    private val storage: PersonalizationAssetStorage,
) {
    fun get(id: PersonalizationAssetId): PersonalizationAsset? = repository.findById(id)

    fun load(asset: PersonalizationAsset): PersonalizationAssetImage? = storage.load(asset.objectKey)
}
