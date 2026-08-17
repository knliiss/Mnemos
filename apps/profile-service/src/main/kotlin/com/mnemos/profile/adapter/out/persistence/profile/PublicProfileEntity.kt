package com.mnemos.profile.adapter.out.persistence.profile

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "public_profiles")
class PublicProfileEntity(
    @Id
    @Column(name = "account_id", nullable = false)
    var accountId: UUID,
    @Column(name = "cristalix_uuid", nullable = false, unique = true)
    var cristalixUuid: UUID,
    @Column(name = "cristalix_nickname", nullable = false, length = 64)
    var cristalixNickname: String,
    @Column(name = "likes", nullable = false)
    var likes: Long,
    @Column(name = "dislikes", nullable = false)
    var dislikes: Long,
    @Column(name = "comments", nullable = false)
    var comments: Long,
    @Column(name = "social_revision", nullable = false)
    var socialRevision: Long,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
