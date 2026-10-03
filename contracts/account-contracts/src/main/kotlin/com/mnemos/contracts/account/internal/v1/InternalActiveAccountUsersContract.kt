package com.mnemos.contracts.account.internal.v1

import java.util.UUID

data class InternalActiveAccountUsersResponse(
    val users: List<InternalActiveAccountUserResponse>,
)

data class InternalActiveAccountUserResponse(
    val userId: UUID,
    val telegramId: Long,
)
