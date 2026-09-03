package com.mnemos.account.application.service.account

import com.mnemos.account.application.event.account.AccountIntegrationEvents
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReactivateAccountService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val integrationEventPublisher: IntegrationEventPublisher,
) {
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun reactivate(accountId: AccountId) {
        val account =
            accountRepository.findByIdForUpdate(accountId)
                ?: throw AccountNotFoundException(accountId)

        if (account.status == AccountStatus.ACTIVE) {
            return
        }

        val now = Instant.now()
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        account.reactivate(now)
        val updatedAccount = accountRepository.update(account)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.activated(
                account = updatedAccount,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }
}
