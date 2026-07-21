package com.mnemos.accountservice.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,

    @Column(name = "owner_user_id", nullable = false, updatable = false)
    var ownerUserId: UUID,

    @Column(name = "cristalix_uuid", nullable = false, unique = true, updatable = false)
    var cristalixUuid: UUID,

    @Column(name = "cristalix_nickname", nullable = false)
    var cristalixNickname: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant
)