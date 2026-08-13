package com.mnemos.gateway.moderation

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class BanCache {
    private val bansByUserId = ConcurrentHashMap<UUID, BanState>()

    fun replace(bans: Collection<ActiveBanResponse>) {
        bansByUserId.clear()

        bans.forEach { ban ->
            activate(
                sanctionId = ban.sanctionId,
                userId = ban.userId,
                occurredAt = ban.occurredAt,
            )
        }
    }

    fun activate(
        sanctionId: UUID,
        userId: UUID,
        occurredAt: Instant,
    ) {
        bansByUserId.compute(userId) { _, current ->
            if (current == null || !occurredAt.isBefore(current.occurredAt)) {
                BanState(
                    sanctionId = sanctionId,
                    occurredAt = occurredAt,
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

    fun isBanned(userId: UUID): Boolean = bansByUserId.containsKey(userId)
}
