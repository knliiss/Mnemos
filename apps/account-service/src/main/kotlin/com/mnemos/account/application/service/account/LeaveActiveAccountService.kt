package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.event.account.AccountIntegrationEvents
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.AccountOwnerCannotLeaveException
import com.mnemos.account.domain.exception.account.ActiveAccountNotSetException
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LeaveActiveAccountService(
    private val userRepository: UserRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val accountContextProvider: ActiveAccountContextProvider,
    private val integrationEventPublisher: IntegrationEventPublisher,
) {
    @Transactional
    fun leaveActiveAccount() {
        val user = currentUserProvider.get()
        val accountId =
            user.activeAccountId
                ?: throw ActiveAccountNotSetException(user.id)

        val context =
            try {
                accountContextProvider.requireActiveContext(accountId)
            } catch (ex: AccountNotFoundException) {
                user.clearActiveAccount()
                userRepository.update(user)
                throw ex
            }

        val account = context.account
        val member = context.member

        if (account.ownerMemberId == member.id) {
            throw AccountOwnerCannotLeaveException(accountId)
        }

        val now = Instant.now()
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        accountMemberRepository.deleteById(member.id)

        user.clearActiveAccount()
        userRepository.update(user)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.memberLeft(
                account = account,
                member = member,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }
}
