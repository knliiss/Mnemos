package com.mnemos.contracts.account.v1

import java.util.UUID

data class AccountStatusChangedV1(
    val status: String,
    val recipientUserIds: List<UUID>,
)
