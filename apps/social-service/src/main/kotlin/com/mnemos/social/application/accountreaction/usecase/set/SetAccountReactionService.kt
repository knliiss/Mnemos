package com.mnemos.social.application.accountreaction.usecase.set

import com.mnemos.social.application.account.exception.SocialAccountNotFoundException
import com.mnemos.social.application.accountreaction.exception.SelfAccountReactionException
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.summary.AccountSocialSummaryService
import com.mnemos.social.domain.reaction.model.AccountReaction
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class SetAccountReactionService(
    private val repository: AccountReactionRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
    private val summaryService: AccountSocialSummaryService,
    private val clock: Clock,
) : SetAccountReactionUseCase, SetAccountReactionForActorUseCase {
    @Transactional
    override fun execute(command: SetAccountReactionCommand) {
        change(command, actorProvider.requireActor())
    }

    @Transactional
    override fun executeForActor(command: SetAccountReactionCommand, actor: SocialActor) {
        change(command, actor)
    }

    private fun change(command: SetAccountReactionCommand, actor: SocialActor) {
        if (actor.accountId == command.targetAccountId) {
            throw SelfAccountReactionException(command.targetAccountId)
        }

        if (!accountAccessPort.isActiveAccount(command.targetAccountId)) {
            throw SocialAccountNotFoundException(command.targetAccountId)
        }

        val now = clock.instant()

        summaryService.lockAccount(command.targetAccountId, now)

        val existing = repository.find(actor.accountId, command.targetAccountId)
        val previous = existing?.type

        if (existing == null) {
            repository.save(
                AccountReaction(
                    actorAccountId = actor.accountId,
                    targetAccountId = command.targetAccountId,
                    type = command.type,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else if (existing.type != command.type) {
            existing.type = command.type
            existing.updatedAt = now

            repository.save(existing)
        }

        summaryService.reactionChanged(
            accountId = command.targetAccountId,
            actorUserId = actor.userId,
            previous = previous,
            current = command.type,
            now = now,
        )
    }
}
