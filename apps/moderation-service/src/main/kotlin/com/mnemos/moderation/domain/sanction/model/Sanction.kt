package com.mnemos.moderation.domain.sanction.model

import com.mnemos.common.core.model.id.UserId
import java.time.Instant

class Sanction private constructor(
    val id: SanctionId,
    val userId: UserId,
    val type: DomainSanctionType,
    val reason: String,
    val internalNote: String?,
    val createdByUserId: UserId,
    val createdAt: Instant,
    val expiresAt: Instant?,
    status: DomainSanctionStatus,
) {
    var status: DomainSanctionStatus = status
        private set

    companion object {
        fun create(
            userId: UserId,
            type: DomainSanctionType,
            reason: String,
            internalNote: String?,
            createdByUserId: UserId,
            createdAt: Instant,
            expiresAt: Instant?,
        ): Sanction {
            require(reason.isNotBlank())
            require(expiresAt == null || expiresAt.isAfter(createdAt))

            return Sanction(
                id = SanctionId.generate(),
                userId = userId,
                type = type,
                reason = reason.trim(),
                internalNote = internalNote?.trim()?.takeIf(String::isNotBlank),
                createdByUserId = createdByUserId,
                createdAt = createdAt,
                expiresAt = expiresAt,
                status = DomainSanctionStatus.ACTIVE,
            )
        }

        fun restore(
            id: SanctionId,
            userId: UserId,
            type: DomainSanctionType,
            reason: String,
            internalNote: String?,
            createdByUserId: UserId,
            createdAt: Instant,
            expiresAt: Instant?,
            status: DomainSanctionStatus,
        ): Sanction =
            Sanction(
                id = id,
                userId = userId,
                type = type,
                reason = reason,
                internalNote = internalNote,
                createdByUserId = createdByUserId,
                createdAt = createdAt,
                expiresAt = expiresAt,
                status = status,
            )
    }

    fun revoke(): Boolean {
        if (status != DomainSanctionStatus.ACTIVE) {
            return false
        }

        status = DomainSanctionStatus.REVOKED
        return true
    }

    fun expire(now: Instant): Boolean {
        if (status != DomainSanctionStatus.ACTIVE) {
            return false
        }

        val expiration = expiresAt ?: return false

        if (expiration.isAfter(now)) {
            return false
        }

        status = DomainSanctionStatus.EXPIRED
        return true
    }
}
