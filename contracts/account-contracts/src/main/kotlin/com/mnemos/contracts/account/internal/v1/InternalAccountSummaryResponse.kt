package com.mnemos.contracts.account.internal.v1

import java.util.UUID

data class InternalAccountSummaryResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
)
