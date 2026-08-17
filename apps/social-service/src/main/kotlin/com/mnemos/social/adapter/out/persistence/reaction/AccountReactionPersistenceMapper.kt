package com.mnemos.social.adapter.out.persistence.reaction

import com.mnemos.social.domain.reaction.model.AccountReaction

object AccountReactionPersistenceMapper {
    fun AccountReaction.toEntity(): AccountReactionEntity =
        AccountReactionEntity(
            actorAccountId = actorAccountId,
            targetAccountId = targetAccountId,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun AccountReactionEntity.toDomain(): AccountReaction =
        AccountReaction(
            actorAccountId = actorAccountId,
            targetAccountId = targetAccountId,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
