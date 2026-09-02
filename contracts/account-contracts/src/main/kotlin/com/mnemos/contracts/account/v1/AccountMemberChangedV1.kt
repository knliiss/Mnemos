package com.mnemos.contracts.account.v1

import java.util.UUID

data class AccountMemberChangedV1(
    val memberId: UUID,
    val userId: UUID,
    val recipientUserIds: List<UUID>,
)
