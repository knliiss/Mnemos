package com.mnemos.account.infrastructure.persistence.projection.account.invite

import java.time.Instant
import java.util.UUID

interface AccountInviteListProjection {
    val id: UUID
    val code: String
    val createdByUserId: UUID
    val createdAt: Instant
    val expiresAt: Instant?
    val consumedAt: Instant?
    val revokedAt: Instant?
}
