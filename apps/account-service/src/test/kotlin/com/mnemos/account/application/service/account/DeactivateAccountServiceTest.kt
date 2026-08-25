package com.mnemos.account.application.service.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DeactivateAccountServiceTest {
    @Test
    fun `admin deactivation clears active account references and persists account`() {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val userRepository = mock<UserRepository>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(accountRepository.update(account)).thenReturn(account)

        val service =
            DeactivateAccountService(
                accountRepository = accountRepository,
                userRepository = userRepository,
            )

        service.deactivate(account.id)

        assertEquals(AccountStatus.DEACTIVATED, account.status)

        verify(userRepository).clearActiveAccountByAccountId(
            accountId = account.id,
            now = account.deactivatedAt!!,
        )
        verify(accountRepository).update(account)
    }

    @Test
    fun `admin deactivation is idempotent for already deactivated account`() {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val userRepository = mock<UserRepository>()

        account.deactivate()
        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)

        val service =
            DeactivateAccountService(
                accountRepository = accountRepository,
                userRepository = userRepository,
            )

        service.deactivate(account.id)

        assertEquals(AccountStatus.DEACTIVATED, account.status)
        verify(accountRepository, never()).update(any())
        verify(userRepository, never()).clearActiveAccountByAccountId(any(), any())
    }

    private fun createAccount(): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = AccountMemberId.generate(),
            cristalixUuid = CristalixUuid(UUID.fromString("11111111-1111-1111-1111-111111111111")),
            cristalixNickname = CristalixNickname("knaliz"),
        )
}
