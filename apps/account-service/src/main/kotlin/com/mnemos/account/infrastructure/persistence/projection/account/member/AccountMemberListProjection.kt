package com.mnemos.account.infrastructure.persistence.projection.account.member

import java.time.Instant
import java.util.UUID

interface AccountMemberListProjection {
    val id: UUID
    val name: String
    val owner: Boolean
    val joinedAt: Instant

    val highestRoleId: UUID?
    val highestRoleName: String?
    val highestRoleColor: String?
    val highestRoleRank: Long?
}
