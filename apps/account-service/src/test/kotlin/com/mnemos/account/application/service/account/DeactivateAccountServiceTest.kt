package com.mnemos.account.application.service.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.account.v1.AccountStatusChangedV1
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class DeactivateAccountServiceTest {
    @Test
    fun `admin deactivation clears active account references persists account and publishes lifecycle event`() {
        val account = createAccount()
        val recipientUserId = UserId.generate()
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val userRepository = mock<UserRepository>()
        val integrationEventPublisher = mock<IntegrationEventPublisher>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(accountMemberRepository.findUserIdsByAccountId(account.id))
            .thenReturn(listOf(recipientUserId))
        whenever(accountRepository.update(account)).thenReturn(account)

        val service =
            DeactivateAccountService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                userRepository = userRepository,
                integrationEventPublisher = integrationEventPublisher,
            )

        service.deactivate(account.id)

        assertEquals(AccountStatus.DEACTIVATED, account.status)

        verify(accountMemberRepository).findUserIdsByAccountId(account.id)
        verify(userRepository).clearActiveAccountByAccountId(
            accountId = account.id,
            now = account.deactivatedAt!!,
        )
        verify(accountRepository).update(account)
        verify(integrationEventPublisher).publish<AccountStatusChangedV1>(any(), any())
    }

    @Test
    fun `admin deactivation is idempotent for already deactivated account`() {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val userRepository = mock<UserRepository>()
        val integrationEventPublisher = mock<IntegrationEventPublisher>()

        account.deactivate()
        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)

        val service =
            DeactivateAccountService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                userRepository = userRepository,
                integrationEventPublisher = integrationEventPublisher,
            )

        service.deactivate(account.id)

        assertEquals(AccountStatus.DEACTIVATED, account.status)
        verify(accountRepository, never()).update(any())
        verify(userRepository, never()).clearActiveAccountByAccountId(any(), any())
        verifyNoInteractions(accountMemberRepository, integrationEventPublisher)
    }

    private fun createAccount(): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = AccountMemberId.generate(),
            cristalixUuid = CristalixUuid(UUID.fromString("11111111-1111-1111-1111-111111111111")),
            cristalixNickname = CristalixNickname("knaliz"),
        )
}
