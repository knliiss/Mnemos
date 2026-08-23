package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.domain.exception.account.AccountUnavailableException
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class ChangeActiveAccountServiceTest {
    @Test
    fun `changing active account rejects deactivated account before membership or user mutation`() {
        val accountId = AccountId.generate()
        val ownerMemberId = AccountMemberId.generate()
        val account = createAccount(accountId, ownerMemberId)
        val user = createUser()
        val userRepository = mock<UserRepository>()
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val currentUserProvider = mock<CurrentUserProvider>()

        account.deactivate()

        whenever(currentUserProvider.get()).thenReturn(user)
        whenever(accountRepository.findByIdForUpdate(accountId)).thenReturn(account)

        val service =
            ChangeActiveAccountService(
                userRepository = userRepository,
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                currentUserProvider = currentUserProvider,
            )

        assertThrows<AccountUnavailableException> {
            service.changeActiveAccount(accountId)
        }

        verify(accountRepository).findByIdForUpdate(accountId)
        verifyNoInteractions(accountMemberRepository, userRepository)
    }

    @Test
    fun `changing active account locks active account and persists user after membership validation`() {
        val accountId = AccountId.generate()
        val ownerMemberId = AccountMemberId.generate()
        val account = createAccount(accountId, ownerMemberId)
        val user = createUser()
        val member =
            AccountMember.create(
                id = ownerMemberId,
                accountId = accountId,
                userId = user.id,
            )
        val userRepository = mock<UserRepository>()
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val currentUserProvider = mock<CurrentUserProvider>()

        whenever(currentUserProvider.get()).thenReturn(user)
        whenever(accountRepository.findByIdForUpdate(accountId)).thenReturn(account)
        whenever(
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = user.id,
            ),
        ).thenReturn(member)

        val service =
            ChangeActiveAccountService(
                userRepository = userRepository,
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                currentUserProvider = currentUserProvider,
            )

        service.changeActiveAccount(accountId)

        verify(accountRepository).findByIdForUpdate(accountId)
        verify(userRepository).update(user)
    }

    private fun createAccount(
        accountId: AccountId,
        ownerMemberId: AccountMemberId,
    ): Account =
        Account.create(
            id = accountId,
            ownerMemberId = ownerMemberId,
            cristalixUuid = CristalixUuid(UUID.fromString("11111111-1111-1111-1111-111111111111")),
            cristalixNickname = CristalixNickname("knaliz"),
        )

    private fun createUser(): User =
        User.create(
            telegramId = TelegramId(931458258),
            username = Username("knaliz"),
            keycloakId = KeycloakId(UUID.fromString("39f20154-d86d-41c5-9659-6986437905bd")),
        )
}
