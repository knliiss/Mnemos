package com.mnemos.contracts.playerdirectory.v1

import java.time.Instant
import java.util.UUID

data class PlayerNicknameChangedV1(
    val playerId: UUID,
    val previousNickname: String,
    val nickname: String,
    val externalVersion: Long,
    val observedAt: Instant,
)
