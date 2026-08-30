package com.mnemos.contracts.account.internal.v1

import java.util.UUID

data class InternalTelegramAccountContextResponse(
    val activeAccountId: UUID?,
    val accounts: List<InternalTelegramAccountSummaryResponse>,
)

data class InternalTelegramAccountSummaryResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
    val status: InternalTelegramAccountStatus,
)

enum class InternalTelegramAccountStatus {
    ACTIVE,
    DEACTIVATED,
}

data class ChangeInternalTelegramActiveAccountRequest(
    val accountId: UUID,
)
