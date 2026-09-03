package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.event.account.AccountIntegrationEvents
import com.mnemos.account.domain.exception.account.AccountOwnerRequiredException
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TransferAccountOwnerShipService(
    private val contextProvider: ActiveAccountContextProvider,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val integrationEventPublisher: IntegrationEventPublisher,
) {
    @Transactional
    fun transfer(
        newOwnerMemberId: AccountMemberId,
        accountId: AccountId,
    ) {
        val context = contextProvider.requireActiveContextForUpdate(accountId)
        val account = context.account

        if (!account.isOwner(context.member.id)) {
            throw AccountOwnerRequiredException(accountId)
        }

        if (account.ownerMemberId == newOwnerMemberId) {
            return
        }

        val newOwnerMember =
            accountMemberRepository.findByIdAndAccountId(
                memberId = newOwnerMemberId,
                accountId = accountId,
            ) ?: throw AccountMemberNotFoundException(
                accountId = accountId,
                memberId = newOwnerMemberId,
            )

        val now = Instant.now()
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        if (!account.transferOwnership(newOwnerMember.id, now)) {
            return
        }

        val updatedAccount = accountRepository.update(account)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.ownershipTransferred(
                account = updatedAccount,
                previousOwner = context.member,
                newOwner = newOwnerMember,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }
}
