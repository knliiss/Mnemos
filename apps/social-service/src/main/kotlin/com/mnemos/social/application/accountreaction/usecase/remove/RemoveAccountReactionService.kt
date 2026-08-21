package com.mnemos.social.application.accountreaction.usecase.remove

import com.mnemos.social.application.account.exception.SocialAccountNotFoundException
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class RemoveAccountReactionService(
    private val repository: AccountReactionRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) : RemoveAccountReactionUseCase {
    @Transactional
    override fun execute(targetAccountId: UUID) {
        val actor = actorProvider.requireActor()

        if (!accountAccessPort.isActiveAccount(targetAccountId)) {
            throw SocialAccountNotFoundException(targetAccountId)
        }

        val now = clock.instant()

        summaryService.lockAccount(targetAccountId, now)

        val existing = repository.find(actor.accountId, targetAccountId) ?: return

        repository.delete(
            actorAccountId = actor.accountId,
            targetAccountId = targetAccountId,
        )

        summaryService.reactionRemoved(
            accountId = targetAccountId,
            actorUserId = actor.userId,
            previous = existing.type,
            now = now,
        )
    }
}
