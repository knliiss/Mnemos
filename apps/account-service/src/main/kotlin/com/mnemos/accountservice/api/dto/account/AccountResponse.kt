package com.mnemos.accountservice.api.dto.account

import java.time.Instant
import java.util.UUID

data class AccountResponse(
    val id: UUID,
    val ownerUserId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
