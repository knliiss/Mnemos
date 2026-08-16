package com.mnemos.social.adapter.out.persistence.reaction

import com.mnemos.social.adapter.out.persistence.reaction.AccountReactionPersistenceMapper.toDomain
import com.mnemos.social.adapter.out.persistence.reaction.AccountReactionPersistenceMapper.toEntity
import com.mnemos.social.domain.reaction.model.AccountReaction
import com.mnemos.social.domain.reaction.model.AccountReactionType
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountReactionRepositoryAdapter(
    private val repository: AccountReactionJpaRepository,
) : AccountReactionRepository {
    override fun find(
        actorAccountId: UUID,
        targetAccountId: UUID,
    ): AccountReaction? =
        repository
            .findById(
                AccountReactionEntityId(
                    actorAccountId = actorAccountId,
                    targetAccountId = targetAccountId,
                ),
            ).orElse(null)
            ?.toDomain()

    override fun save(reaction: AccountReaction): AccountReaction = repository.save(reaction.toEntity()).toDomain()

    override fun delete(
        actorAccountId: UUID,
        targetAccountId: UUID,
    ) {
        repository.deleteById(
            AccountReactionEntityId(
                actorAccountId = actorAccountId,
                targetAccountId = targetAccountId,
            ),
        )
    }

    override fun countByTargetAndType(
        targetAccountId: UUID,
        type: AccountReactionType,
    ): Long = repository.countByTargetAccountIdAndType(targetAccountId, type)
}
