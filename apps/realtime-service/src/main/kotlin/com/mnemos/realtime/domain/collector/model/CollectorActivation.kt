package com.mnemos.realtime.domain.collector.model

import java.time.Duration
import java.time.Instant
import java.util.UUID

class CollectorActivation private constructor(
    val id: CollectorActivationId,
    val userId: UUID,
    val tokenHash: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    credentialId: CollectorCredentialId?,
    redeemedAt: Instant?,
) {
    var credentialId: CollectorCredentialId? = credentialId
        private set

    var redeemedAt: Instant? = redeemedAt
        private set

    fun isExpired(now: Instant): Boolean = !expiresAt.isAfter(now)

    fun redeem(
        credentialId: CollectorCredentialId,
        now: Instant,
    ) {
        check(this.credentialId == null && redeemedAt == null) {
            "Collector activation has already been redeemed"
        }
        check(!isExpired(now)) {
            "Expired collector activation cannot be redeemed"
        }

        this.credentialId = credentialId
        redeemedAt = now
    }

    companion object {
        fun create(
            userId: UUID,
            tokenHash: String,
            now: Instant,
            ttl: Duration,
        ): CollectorActivation {
            require(tokenHash.matches(TOKEN_HASH_PATTERN))
            require(!ttl.isZero && !ttl.isNegative)

            return CollectorActivation(
                id = CollectorActivationId.generate(),
                userId = userId,
                tokenHash = tokenHash,
                createdAt = now,
                expiresAt = now.plus(ttl),
                credentialId = null,
                redeemedAt = null,
            )
        }

        fun restore(
            id: CollectorActivationId,
            userId: UUID,
            tokenHash: String,
            createdAt: Instant,
            expiresAt: Instant,
            credentialId: CollectorCredentialId?,
            redeemedAt: Instant?,
        ): CollectorActivation =
            CollectorActivation(
                id = id,
                userId = userId,
                tokenHash = tokenHash,
                createdAt = createdAt,
                expiresAt = expiresAt,
                credentialId = credentialId,
                redeemedAt = redeemedAt,
            )

        private val TOKEN_HASH_PATTERN = Regex("^[0-9a-f]{64}$")
    }
}
