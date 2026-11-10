package com.mnemos.social.application.accountreaction.usecase.get

import com.mnemos.social.application.account.exception.SocialAccountNotFoundException
import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.domain.reaction.model.AccountReactionType
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetAccountReactionService(
    private val repository: AccountReactionRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
) : GetAccountReactionUseCase, GetAccountReactionForActorUseCase {
    @Transactional(readOnly = true)
    override fun execute(targetAccountId: UUID): AccountReactionType? =
        read(targetAccountId, actorProvider.requireActor())

    @Transactional(readOnly = true)
    override fun executeForActor(targetAccountId: UUID, actor: SocialActor): AccountReactionType? =
        read(targetAccountId, actor)

    private fun read(targetAccountId: UUID, actor: SocialActor): AccountReactionType? {

        if (!accountAccessPort.isActiveAccount(targetAccountId)) {
            throw SocialAccountNotFoundException(targetAccountId)
        }

        return repository.find(actor.accountId, targetAccountId)?.type
    }
}
