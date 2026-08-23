package com.mnemos.account.infrastructure.persistence.projection.account

import java.time.Instant
import java.util.UUID

interface AccountSummaryProjection {
    val id: UUID
    val cristalixUuid: UUID
    val cristalixNickname: String
    val currentMemberId: UUID
    val ownerMemberId: UUID
    val memberCount: Long
    val maxMembers: Int
    val status: String
}

interface AccountDetailsProjection {
    val id: UUID
    val cristalixUuid: UUID
    val cristalixNickname: String
    val currentMemberId: UUID
    val ownerMemberId: UUID
    val memberCount: Long
    val maxMembers: Int
    val status: String
    val baseRoleId: UUID?
    val createdAt: Instant
    val updatedAt: Instant
}
