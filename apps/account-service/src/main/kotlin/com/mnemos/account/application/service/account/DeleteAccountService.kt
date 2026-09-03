package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.application.event.account.AccountIntegrationEvents
import com.mnemos.account.domain.exception.account.AccountNotActiveException
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.AccountOwnerRequiredException
import com.mnemos.account.domain.exception.account.member.ActiveAccountMembershipNotFoundException
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DeleteAccountService(
    private val currentUserProvider: CurrentUserProvider,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val userRepository: UserRepository,
    private val integrationEventPublisher: IntegrationEventPublisher,
) {
    @Transactional
    fun deleteAccount(accountId: AccountId) {
        val user = currentUserProvider.get()

        if (user.activeAccountId != accountId) {
            throw AccountNotActiveException(accountId)
        }

        val account =
            accountRepository.findByIdForUpdate(accountId)
                ?: throw AccountNotFoundException(accountId)

        val member =
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = user.id,
            ) ?: throw ActiveAccountMembershipNotFoundException(
                accountId = accountId,
                userId = user.id,
            )

        if (account.ownerMemberId != member.id) {
            throw AccountOwnerRequiredException(accountId)
        }

        val now = Instant.now()
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        account.deletePermanently(now)

        userRepository.clearActiveAccountByAccountId(
            accountId = accountId,
            now = now,
        )

        val deletedAccount = accountRepository.update(account)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.deleted(
                account = deletedAccount,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }
}
