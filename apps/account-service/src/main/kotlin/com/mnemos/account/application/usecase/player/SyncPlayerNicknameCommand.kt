package com.mnemos.account.application.usecase.player

import java.time.Instant
import java.util.UUID

data class SyncPlayerNicknameCommand(
    val playerId: UUID,
    val nickname: String,
    val externalVersion: Long,
    val observedAt: Instant,
)
