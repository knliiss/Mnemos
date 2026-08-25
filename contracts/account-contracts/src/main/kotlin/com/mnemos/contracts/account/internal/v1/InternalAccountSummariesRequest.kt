package com.mnemos.contracts.account.internal.v1

import java.util.UUID

data class InternalAccountSummariesRequest(
    val accountIds: Set<UUID>,
)
