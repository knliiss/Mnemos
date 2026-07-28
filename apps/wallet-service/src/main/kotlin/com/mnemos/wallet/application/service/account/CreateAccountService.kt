package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.api.dto.account.CreateAccountRequest
import com.mnemos.accountservice.application.context.CurrentUserProvider
import com.mnemos.accountservice.application.event.account.AccountIntegrationEvents
import com.mnemos.accountservice.application.result.account.CreateAccountResult
import com.mnemos.accountservice.domain.exception.account.AccountIdentityConflictException
import com.mnemos.accountservice.domain.exception.account.cristalix.CristalixPlayerNotFoundException
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
import com.mnemos.accountservice.integration.cristalix.CristalixIdentityProvider
import com.mnemos.accountservice.integration.cristalix.model.CristalixPlayer
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import com.mnemos.messaging.outbox.writer.OutboxEventWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class CreateAccountService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val userRepository: UserRepository,
    private val cristalixIdentityProvider: CristalixIdentityProvider,
    private val currentUserProvider: CurrentUserProvider,
    private val outboxEventWriter: OutboxEventWriter,
    private val eventMetadataProvider: EventMetadataProvider,
    private val transactionTemplate: TransactionTemplate
) {
    fun createAccount(request: CreateAccountRequest): CreateAccountResult {
        val nickname = CristalixNickname(request.cristalixNickname)

        val player = cristalixIdentityProvider.findByNickname(nickname)
            ?: throw CristalixPlayerNotFoundException(nickname)

        return requireNotNull(
            transactionTemplate.execute {
                createFromPlayer(player)
            }
        ) {
            "Create account transaction returned no result"
        }
    }

    private fun createFromPlayer(player: CristalixPlayer): CreateAccountResult {
        val owner = currentUserProvider.get()

        if (accountRepository.existsByCristalixUuid(player.uuid)) {
            throw AccountIdentityConflictException(player.uuid)
        }

        val accountId = AccountId.generate()
        val ownerMemberId = AccountMemberId.generate()

        val account = Account.create(
            id = accountId,
            ownerMemberId = ownerMemberId,
            cristalixUuid = player.uuid,
            cristalixNickname = player.nickname
        )

        val ownerMember = AccountMember.create(
            id = ownerMemberId,
            accountId = accountId,
            userId = owner.id
        )

        val createdAccount = accountRepository.create(account)
        val createdOwnerMember = accountMemberRepository.create(ownerMember)

        owner.changeActiveAccount(createdAccount.id)
        userRepository.update(owner)

        val metadata = eventMetadataProvider.current().copy(
            actorUserId = owner.id.value
        )

        outboxEventWriter.append(
            destination = EventDestination(
                topic = AccountEventContract.TOPIC,
                key = createdAccount.id.value.toString()
            ),
            event = AccountIntegrationEvents.created(
                account = createdAccount,
                ownerMember = createdOwnerMember
            ),
            metadata = metadata
        )

        return CreateAccountResult(
            accountId = createdAccount.id,
            memberId = createdOwnerMember.id
        )
    }
}