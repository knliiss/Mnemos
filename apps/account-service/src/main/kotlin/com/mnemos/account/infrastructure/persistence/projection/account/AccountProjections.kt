package com.mnemos.account.infrastructure.persistence.projection.account

import java.time.Instant
import java.util.UUID

interface AccountSummaryProjection {
    val id: UUID
    val cristalixNickname: String
    val currentMemberId: UUID
    val ownerMemberId: UUID
    val memberCount: Long
    val maxMembers: Int
}

interface AccountDetailsProjection {
    val id: UUID
    val cristalixUuid: UUID
    val cristalixNickname: String
    val currentMemberId: UUID
    val ownerMemberId: UUID
    val memberCount: Long
    val maxMembers: Int
    val createdAt: Instant
    val updatedAt: Instant
}
