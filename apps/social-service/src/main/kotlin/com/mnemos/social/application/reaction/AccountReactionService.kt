package com.mnemos.social.application.reaction

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.adapter.out.persistence.reaction.AccountReactionEntity
import com.mnemos.social.adapter.out.persistence.reaction.AccountReactionEntityId
import com.mnemos.social.adapter.out.persistence.reaction.AccountReactionJpaRepository
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.reaction.model.AccountReactionType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class AccountReactionService(
    private val repository: AccountReactionJpaRepository,
    private val actorProvider: SocialActorProvider,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) {
    @Transactional
    fun set(
        targetAccountId: UUID,
        type: AccountReactionType,
    ) {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_REACTIONS)

        if (actor.accountId == targetAccountId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "An account cannot react to itself",
            )
        }

        val id = AccountReactionEntityId(actor.accountId, targetAccountId)
        val now = clock.instant()
        val existing = repository.findById(id).orElse(null)
        val previous = existing?.type

        if (existing == null) {
            repository.save(
                AccountReactionEntity(
                    actorAccountId = actor.accountId,
                    targetAccountId = targetAccountId,
                    type = type,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else if (existing.type != type) {
            existing.type = type
            existing.updatedAt = now
            repository.save(existing)
        }

        summaryService.reactionChanged(
            accountId = targetAccountId,
            actorUserId = actor.userId,
            previous = previous,
            current = type,
            now = now,
        )
    }

    @Transactional
    fun remove(targetAccountId: UUID) {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_REACTIONS)
        val id = AccountReactionEntityId(actor.accountId, targetAccountId)
        val existing = repository.findById(id).orElse(null) ?: return
        val now = clock.instant()

        repository.delete(existing)
        summaryService.reactionRemoved(
            accountId = targetAccountId,
            actorUserId = actor.userId,
            previous = existing.type,
            now = now,
        )
    }

    @Transactional(readOnly = true)
    fun summary(targetAccountId: UUID): AccountReactionSummary =
        AccountReactionSummary(
            likes = repository.countByTargetAccountIdAndType(
                targetAccountId,
                AccountReactionType.LIKE,
            ),
            dislikes = repository.countByTargetAccountIdAndType(
                targetAccountId,
                AccountReactionType.DISLIKE,
            ),
        )
}

data class AccountReactionSummary(
    val likes: Long,
    val dislikes: Long,
)
