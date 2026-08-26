package com.mnemos.playerdirectory.adapter.out.persistence.activity

import com.mnemos.playerdirectory.domain.activity.model.PendingPlayerActivity
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

object PendingPlayerActivityPersistenceMapper {
    fun PendingPlayerActivity.toEntity(): PendingPlayerActivityEntity =
        PendingPlayerActivityEntity(
            normalizedNickname = nickname.normalized(),
            nickname = nickname.value,
            pendingDropCount = pendingDropCount,
            lastDropAt = lastDropAt,
            nextAttemptAt = nextAttemptAt,
            leaseUntil = leaseUntil,
            consecutiveFailures = consecutiveFailures,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun PendingPlayerActivityEntity.toDomain(): PendingPlayerActivity =
        PendingPlayerActivity(
            nickname = PlayerNickname.of(nickname),
            pendingDropCount = pendingDropCount,
            lastDropAt = lastDropAt,
            nextAttemptAt = nextAttemptAt,
            leaseUntil = leaseUntil,
            consecutiveFailures = consecutiveFailures,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
