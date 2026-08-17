package com.mnemos.social.contracts.event

import java.time.Instant
import java.util.UUID

data class AccountSocialSummaryChangedV1(
    val accountId: UUID,
    val likes: Long,
    val dislikes: Long,
    val comments: Long,
    val revision: Long,
    val occurredAt: Instant,
)
