package com.mnemos.playerdirectory.application.accountlink.model

import java.time.Instant
import java.util.UUID

data class AccountBootstrapSnapshot(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val createdAt: Instant,
)
