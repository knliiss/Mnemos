package com.mnemos.playerdirectory.application.activity.model

import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import java.time.Instant

data class PendingPlayerActivityLease(
    val nickname: PlayerNickname,
    val pendingDropCount: Long,
    val lastDropAt: Instant,
    val leaseUntil: Instant,
)
