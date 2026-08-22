package com.mnemos.personalization.adapter.out.persistence.badge

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "badges")
class BadgeEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "code", nullable = false, unique = true, length = 32)
    var code: String,
    @Column(name = "name", nullable = false, length = 64)
    var name: String,
    @Column(name = "image_object_key", nullable = false, unique = true, length = 96)
    var imageObjectKey: String,
    @Column(name = "color", nullable = false, length = 7)
    var color: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
