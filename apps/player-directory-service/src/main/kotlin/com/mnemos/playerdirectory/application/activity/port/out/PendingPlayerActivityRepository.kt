package com.mnemos.playerdirectory.application.activity.port.out

import com.mnemos.playerdirectory.domain.activity.model.PendingPlayerActivity
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import java.time.Instant

interface PendingPlayerActivityRepository {
    fun enqueueDrop(
        nickname: PlayerNickname,
        occurredAt: Instant,
        recordedAt: Instant,
    )

    fun enqueueIdentityDemand(
        nickname: PlayerNickname,
        recordedAt: Instant,
    )

    fun save(activity: PendingPlayerActivity): PendingPlayerActivity

    fun delete(activity: PendingPlayerActivity)

    fun findByNicknameForUpdate(nickname: PlayerNickname): PendingPlayerActivity?

    fun findDueForUpdate(
        now: Instant,
        limit: Int,
    ): List<PendingPlayerActivity>
}
