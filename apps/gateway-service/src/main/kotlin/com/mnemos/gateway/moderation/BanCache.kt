package com.mnemos.gateway.moderation

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class BanCache(
    private val clock: Clock,
) {
    private val bansByUserId = ConcurrentHashMap<UUID, BanState>()

    fun replace(bans: Collection<ActiveBanResponse>) {
        bansByUserId.clear()

        bans.forEach { ban ->
            activate(
                sanctionId = ban.sanctionId,
                userId = ban.userId,
                reason = ban.reason,
                occurredAt = ban.occurredAt,
                expiresAt = ban.expiresAt,
            )
        }
    }

    fun activate(
        sanctionId: UUID,
        userId: UUID,
        reason: String,
        occurredAt: Instant,
        expiresAt: Instant?,
    ) {
        bansByUserId.compute(userId) { _, current ->
            if (current == null || !occurredAt.isBefore(current.occurredAt)) {
                BanState(
                    sanctionId = sanctionId,
                    reason = reason,
                    occurredAt = occurredAt,
                    expiresAt = expiresAt,
                )
            } else {
                current
            }
        }
    }

    fun deactivate(
        sanctionId: UUID,
        userId: UUID,
        occurredAt: Instant,
    ) {
        bansByUserId.computeIfPresent(userId) { _, current ->
            if (current.sanctionId == sanctionId && !occurredAt.isBefore(current.occurredAt)) {
                null
            } else {
                current
            }
        }
    }

    fun findActiveBan(userId: UUID): BanState? {
        val ban = bansByUserId[userId] ?: return null
        val expiresAt = ban.expiresAt

        if (expiresAt != null && !expiresAt.isAfter(clock.instant())) {
            bansByUserId.remove(userId, ban)
            return null
        }

        return ban
    }

    fun isBanned(userId: UUID): Boolean = findActiveBan(userId) != null
}
