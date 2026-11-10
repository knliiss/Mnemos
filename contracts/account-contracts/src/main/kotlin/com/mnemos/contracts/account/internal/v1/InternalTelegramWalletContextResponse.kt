package com.mnemos.contracts.account.internal.v1

import java.util.UUID

data class InternalTelegramWalletContextResponse(
    val userId: UUID,
    val activeAccountId: UUID?,
)
