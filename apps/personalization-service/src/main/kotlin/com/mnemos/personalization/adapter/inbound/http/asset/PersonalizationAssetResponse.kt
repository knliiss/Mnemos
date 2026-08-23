package com.mnemos.personalization.adapter.inbound.http.asset

import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import java.time.Instant
import java.util.UUID

data class PersonalizationAssetResponse(
    val id: UUID,
    val contentUrl: String,
    val contentType: String,
    val sizeBytes: Long,
    val widthPx: Int,
    val heightPx: Int,
    val createdAt: Instant,
)

object PersonalizationAssetApiMapper {
    fun PersonalizationAsset.toResponse(): PersonalizationAssetResponse =
        PersonalizationAssetResponse(
            id = id.id,
            contentUrl = "/api/v1/personalizations/assets/${id.id}/content",
            contentType = contentType,
            sizeBytes = sizeBytes,
            widthPx = widthPx,
            heightPx = heightPx,
            createdAt = createdAt,
        )
}
