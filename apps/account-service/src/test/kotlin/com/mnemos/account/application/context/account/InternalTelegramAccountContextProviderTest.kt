package com.mnemos.account.application.context.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class InternalTelegramAccountContextProviderTest {
    private val userRepository = mock<UserRepository>()
    private val accountRepository = mock<AccountRepository>()
    private val accountMemberRepository = mock<AccountMemberRepository>()

    private val provider =
        InternalTelegramAccountContextProvider(
            userRepository = userRepository,
            accountRepository = accountRepository,
            accountMemberRepository = accountMemberRepository,
        )

    @Test
    fun `missing telegram user does not resolve account context`() {
        whenever(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(null)

        val context = provider.requirePermission(TELEGRAM_ID, RolePermission.MANAGE_MEMBERS)

        assertNull(context)
        verifyNoInteractions(accountRepository, accountMemberRepository)
    }

    @Test
    fun `read permission is checked against telegram users active account`() {
        val user = mock<User>()
        val account = mock<Account>()
        val member = mock<AccountMember>()

        stubContext(user, account, member)
        whenever(accountRepository.findByIdIncludingInactive(ACCOUNT_ID)).thenReturn(account)
        whenever(
            accountMemberRepository.hasEffectivePermission(
                memberId = MEMBER_ID,
                permission = RolePermission.MANAGE_MEMBERS,
            ),
        ).thenReturn(true)

        val context = provider.requirePermission(TELEGRAM_ID, RolePermission.MANAGE_MEMBERS)

        assertSame(user, context?.user)
        assertSame(account, context?.account)
        assertSame(member, context?.member)
        verify(accountRepository).findByIdIncludingInactive(ACCOUNT_ID)
    }

    @Test
    fun `update permission locks telegram users active account`() {
        val user = mock<User>()
        val account = mock<Account>()
        val member = mock<AccountMember>()

        stubContext(user, account, member)
        whenever(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(account)
        whenever(
            accountMemberRepository.hasEffectivePermission(
                memberId = MEMBER_ID,
                permission = RolePermission.MANAGE_MEMBERS,
            ),
        ).thenReturn(true)

        val context = provider.requirePermissionForUpdate(TELEGRAM_ID, RolePermission.MANAGE_MEMBERS)

        assertEquals(ACCOUNT_ID, context?.account?.id)
        verify(accountRepository).findByIdForUpdate(ACCOUNT_ID)
    }

    @Test
    fun `telegram context does not bypass account permissions`() {
        val user = mock<User>()
        val account = mock<Account>()
        val member = mock<AccountMember>()

        stubContext(user, account, member)
        whenever(accountRepository.findByIdIncludingInactive(ACCOUNT_ID)).thenReturn(account)
        whenever(
            accountMemberRepository.hasEffectivePermission(
                memberId = MEMBER_ID,
                permission = RolePermission.MANAGE_MEMBERS,
            ),
        ).thenReturn(false)

        assertThrows(AccountPermissionDeniedException::class.java) {
            provider.requirePermission(TELEGRAM_ID, RolePermission.MANAGE_MEMBERS)
        }
    }

    private fun stubContext(
        user: User,
        account: Account,
        member: AccountMember,
    ) {
        whenever(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(user)
        whenever(user.id).thenReturn(USER_ID)
        whenever(user.activeAccountId).thenReturn(ACCOUNT_ID)
        whenever(account.id).thenReturn(ACCOUNT_ID)
        whenever(account.status).thenReturn(AccountStatus.ACTIVE)
        whenever(member.id).thenReturn(MEMBER_ID)
        whenever(
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = ACCOUNT_ID,
                userId = USER_ID,
            ),
        ).thenReturn(member)
    }

    private companion object {
        val TELEGRAM_ID = TelegramId(931458258L)
        val ACCOUNT_ID = AccountId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"))
        val USER_ID = UserId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72"))
        val MEMBER_ID = AccountMemberId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"))
    }
}
