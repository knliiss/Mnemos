package com.mnemos.accountservice.api.dto.account

import java.util.UUID

data class AvailableAccountResponse(
    val id: UUID,
    val ownerUserId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
)
