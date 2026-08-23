package com.mnemos.personalization.adapter.out.persistence.asset

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId

object PersonalizationAssetPersistenceMapper {
    fun PersonalizationAsset.toEntity(): PersonalizationAssetEntity =
        PersonalizationAssetEntity(
            id = id.id,
            ownerUserId = ownerUserId.id,
            objectKey = objectKey,
            contentType = contentType,
            sizeBytes = sizeBytes,
            widthPx = widthPx,
            heightPx = heightPx,
            createdAt = createdAt,
        )

    fun PersonalizationAssetEntity.toDomain(): PersonalizationAsset =
        PersonalizationAsset(
            id = PersonalizationAssetId(id),
            ownerUserId = UserId(ownerUserId),
            objectKey = objectKey,
            contentType = contentType,
            sizeBytes = sizeBytes,
            widthPx = widthPx,
            heightPx = heightPx,
            createdAt = createdAt,
        )
}
