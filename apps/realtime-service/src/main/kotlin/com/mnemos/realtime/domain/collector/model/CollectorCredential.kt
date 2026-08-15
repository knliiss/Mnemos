package com.mnemos.realtime.domain.collector.model

import java.time.Instant

class CollectorCredential private constructor(
    val id: CollectorCredentialId,
    val name: String,
    secretHash: String,
    enabled: Boolean,
    val createdAt: Instant,
    updatedAt: Instant,
    revokedAt: Instant?,
) {
    var secretHash: String = secretHash
        private set

    var enabled: Boolean = enabled
        private set

    var updatedAt: Instant = updatedAt
        private set

    var revokedAt: Instant? = revokedAt
        private set

    fun rotate(
        newSecretHash: String,
        now: Instant,
    ) {
        check(revokedAt == null) {
            "Revoked collector credential cannot be rotated"
        }

        secretHash = newSecretHash
        enabled = true
        updatedAt = now
    }

    fun enable(now: Instant) {
        check(revokedAt == null) {
            "Revoked collector credential cannot be enabled"
        }

        enabled = true
        updatedAt = now
    }

    fun disable(now: Instant) {
        enabled = false
        updatedAt = now
    }

    fun revoke(now: Instant) {
        enabled = false
        updatedAt = now
        revokedAt = now
    }

    companion object {
        fun create(
            name: String,
            secretHash: String,
            now: Instant,
        ): CollectorCredential {
            require(name.isNotBlank())
            require(secretHash.matches(SECRET_HASH_PATTERN))

            return CollectorCredential(
                id = CollectorCredentialId.generate(),
                name = name.trim(),
                secretHash = secretHash,
                enabled = true,
                createdAt = now,
                updatedAt = now,
                revokedAt = null,
            )
        }

        fun restore(
            id: CollectorCredentialId,
            name: String,
            secretHash: String,
            enabled: Boolean,
            createdAt: Instant,
            updatedAt: Instant,
            revokedAt: Instant?,
        ): CollectorCredential =
            CollectorCredential(
                id = id,
                name = name,
                secretHash = secretHash,
                enabled = enabled,
                createdAt = createdAt,
                updatedAt = updatedAt,
                revokedAt = revokedAt,
            )

        private val SECRET_HASH_PATTERN = Regex("^[0-9a-f]{64}$")
    }
}
