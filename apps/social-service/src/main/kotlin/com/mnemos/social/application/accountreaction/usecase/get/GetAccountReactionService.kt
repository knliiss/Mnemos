package com.mnemos.social.application.accountreaction.usecase.get

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.domain.reaction.model.AccountReactionType
import com.mnemos.social.domain.reaction.repository.AccountReactionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class GetAccountReactionService(
    private val repository: AccountReactionRepository,
    private val actorProvider: SocialActorProvider,
    private val accountAccessPort: AccountAccessPort,
) : GetAccountReactionUseCase {
    @Transactional(readOnly = true)
    override fun execute(targetAccountId: UUID): AccountReactionType? {
        val actor = actorProvider.requireActor()

        if (!accountAccessPort.isActiveAccount(targetAccountId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Target account was not found")
        }

        return repository.find(actor.accountId, targetAccountId)?.type
    }
}
