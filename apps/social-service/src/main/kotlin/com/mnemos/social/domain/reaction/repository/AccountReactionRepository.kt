package com.mnemos.social.domain.reaction.repository

import com.mnemos.social.domain.reaction.model.AccountReaction
import com.mnemos.social.domain.reaction.model.AccountReactionType
import java.util.UUID

interface AccountReactionRepository {
    fun find(
        actorAccountId: UUID,
        targetAccountId: UUID,
    ): AccountReaction?

    fun save(reaction: AccountReaction): AccountReaction

    fun delete(
        actorAccountId: UUID,
        targetAccountId: UUID,
    )

    fun countByTargetAndType(
        targetAccountId: UUID,
        type: AccountReactionType,
    ): Long
}
