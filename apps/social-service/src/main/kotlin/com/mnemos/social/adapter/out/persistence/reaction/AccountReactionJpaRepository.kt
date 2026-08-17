package com.mnemos.social.adapter.out.persistence.reaction

import com.mnemos.social.domain.reaction.model.AccountReactionType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccountReactionJpaRepository : JpaRepository<AccountReactionEntity, AccountReactionEntityId> {
    fun countByTargetAccountIdAndType(
        targetAccountId: UUID,
        type: AccountReactionType,
    ): Long
}
