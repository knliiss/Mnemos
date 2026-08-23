package com.mnemos.personalization.adapter.out.persistence.asset

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "personalization_assets")
class PersonalizationAssetEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "owner_user_id", nullable = false)
    var ownerUserId: UUID,
    @Column(name = "object_key", nullable = false, length = 160)
    var objectKey: String,
    @Column(name = "content_type", nullable = false, length = 64)
    var contentType: String,
    @Column(name = "size_bytes", nullable = false)
    var sizeBytes: Long,
    @Column(name = "width_px", nullable = false)
    var widthPx: Int,
    @Column(name = "height_px", nullable = false)
    var heightPx: Int,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
