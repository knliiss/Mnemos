package com.mnemos.contracts.account.v1

import java.util.UUID

data class AccountOwnershipTransferredV1(
    val previousOwnerMemberId: UUID,
    val previousOwnerUserId: UUID,
    val newOwnerMemberId: UUID,
    val newOwnerUserId: UUID,
    val recipientUserIds: List<UUID>,
)
