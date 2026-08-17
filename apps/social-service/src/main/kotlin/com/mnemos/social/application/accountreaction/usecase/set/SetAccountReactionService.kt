package com.mnemos.social.application.accountreaction.usecase.set

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.reaction.model.AccountReaction
import com.mnemos.social.domain.reaction.model.AccountReactionType
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.util.UUID

@Service
class SetAccountReactionService(
    private val repository: AccountReactionRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) : SetAccountReactionUseCase {
    @Transactional
    override fun execute(
        targetAccountId: UUID,
        type: AccountReactionType,
    ) {
        val actor = actorProvider.requireActor(RolePermission.PUBLISH_REACTIONS)

        if (actor.accountId == targetAccountId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "An account cannot react to itself")
        }

        if (!accountAccessPort.isActiveAccount(targetAccountId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Target account was not found")
        }

        val now = clock.instant()
        summaryService.lockAccount(targetAccountId, now)
        val existing = repository.find(actor.accountId, targetAccountId)
        val previous = existing?.type

        if (existing == null) {
            repository.save(
                AccountReaction(
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
}
