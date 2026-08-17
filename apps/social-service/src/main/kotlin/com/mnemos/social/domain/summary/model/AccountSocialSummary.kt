package com.mnemos.social.domain.summary.model

import java.time.Instant
import java.util.UUID

data class AccountSocialSummary(
    val accountId: UUID,
    var likes: Long,
    var dislikes: Long,
    var comments: Long,
    var revision: Long,
    var updatedAt: Instant,
)
