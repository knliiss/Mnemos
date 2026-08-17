package com.mnemos.social.domain.reaction.model

import java.time.Instant
import java.util.UUID

data class AccountReaction(
    val actorAccountId: UUID,
    val targetAccountId: UUID,
    var type: AccountReactionType,
    val createdAt: Instant,
    var updatedAt: Instant,
)
