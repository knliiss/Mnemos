package com.mnemos.social.application.spotlight.model

import java.util.UUID

data class SpotlightWalletResult(
    val requestId: UUID,
    val spotlightId: UUID,
    val ownerUserId: UUID,
    val amount: Long,
)
