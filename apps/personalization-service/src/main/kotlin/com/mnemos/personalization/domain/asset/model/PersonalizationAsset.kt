package com.mnemos.personalization.domain.asset.model

import com.mnemos.common.core.model.id.UserId
import java.time.Instant

data class PersonalizationAsset(
    val id: PersonalizationAssetId,
    val ownerUserId: UserId,
    val objectKey: String,
    val contentType: String,
    val sizeBytes: Long,
    val widthPx: Int,
    val heightPx: Int,
    val createdAt: Instant,
) {
    init {
        require(objectKey.isNotBlank()) { "Personalization asset object key cannot be blank" }
        require(contentType.isNotBlank()) { "Personalization asset content type cannot be blank" }
        require(sizeBytes > 0) { "Personalization asset size must be positive" }
        require(widthPx > 0) { "Personalization asset width must be positive" }
        require(heightPx > 0) { "Personalization asset height must be positive" }
    }
}
