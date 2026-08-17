package com.mnemos.profile.application.profile.model

import java.time.Instant
import java.util.UUID

data class AccountBootstrapSnapshot(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val createdAt: Instant,
)
