package com.mnemos.contracts.account.internal.v1

import java.time.Instant
import java.util.UUID

data class InternalAccountBootstrapResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val createdAt: Instant,
)
