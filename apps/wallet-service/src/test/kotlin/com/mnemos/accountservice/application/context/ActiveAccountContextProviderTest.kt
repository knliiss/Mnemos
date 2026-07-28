package com.mnemos.accountservice.application.context

import com.mnemos.accountservice.domain.exception.account.AccountNotActiveException
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.exception.account.AccountUnavailableException
import com.mnemos.accountservice.domain.exception.account.member.ActiveAccountMembershipNotFoundException
import com.mnemos.accountservice.domain.model.account.AccountStatus
import com.mnemos.accountservice.support.InMemoryAccountMemberRepository
import com.mnemos.accountservice.support.InMemoryAccountRepository
import com.mnemos.accountservice.support.InMemoryUserRepository
import com.mnemos.accountservice.support.SecurityContextTest
import com.mnemos.accountservice.support.account
import com.mnemos.accountservice.support.activeAccountContextProvider
import com.mnemos.accountservice.support.authenticate
import com.mnemos.accountservice.support.member
import com.mnemos.accountservice.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class ActiveAccountContextProviderTest : SecurityContextTest() {
    @Test
    fun `builds active context for matching account and membership`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId, ownerMemberId = member.id)
        val provider = provider(user, account, member)

        val context = provider.requireActiveContext(accountId)

        assertEquals(user, context.user)
        assertEquals(account, context.account)
        assertEquals(member, context.member)
    }

    @Test
    fun `rejects account that is not selected as active`() {
        val activeId = AccountId(UUID.randomUUID())
        val requestedId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = activeId)
        authenticate(user)
        val provider =
            activeAccountContextProvider(
                InMemoryUserRepository(listOf(user)),
                InMemoryAccountRepository(),
                InMemoryAccountMemberRepository(),
            )

        assertThrows<AccountNotActiveException> { provider.requireActiveContext(requestedId) }
    }

    @Test
    fun `throws when active account no longer exists`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        authenticate(user)
        val provider =
            activeAccountContextProvider(
                InMemoryUserRepository(listOf(user)),
                InMemoryAccountRepository(),
                InMemoryAccountMemberRepository(),
            )

        assertThrows<AccountNotFoundException> { provider.requireActiveContext(accountId) }
    }

    @Test
    fun `rejects deleting and deleted accounts`() {
        listOf(AccountStatus.DELETING, AccountStatus.DELETED).forEach { status ->
            val accountId = AccountId(UUID.randomUUID())
            val user = user(activeAccountId = accountId)
            val member = member(accountId = accountId, userId = user.id)
            val account =
                account(
                    id = accountId,
                    ownerMemberId = member.id,
                    status = status,
                    deletionRequestedAt = if (status != AccountStatus.ACTIVE) com.mnemos.accountservice.support.TEST_TIME else null,
                    deletedAt = if (status == AccountStatus.DELETED) com.mnemos.accountservice.support.TEST_TIME else null,
                )
            val provider = provider(user, account, member)

            assertThrows<AccountUnavailableException> { provider.requireActiveContext(accountId) }
        }
    }

    @Test
    fun `throws when membership is missing`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val account = account(id = accountId)
        authenticate(user)
        val provider =
            activeAccountContextProvider(
                InMemoryUserRepository(listOf(user)),
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(),
            )

        assertThrows<ActiveAccountMembershipNotFoundException> { provider.requireActiveContext(accountId) }
    }

    @Test
    fun `permission check returns context when permission is effective`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId, ownerMemberId = member.id)
        val memberRepository = InMemoryAccountMemberRepository(listOf(member))
        memberRepository.permissions[member.id] = mutableSetOf(RolePermission.CREATE_ROLES)
        authenticate(user)
        val provider =
            activeAccountContextProvider(
                InMemoryUserRepository(listOf(user)),
                InMemoryAccountRepository(listOf(account)),
                memberRepository,
            )

        assertEquals(account, provider.requirePermission(accountId, RolePermission.CREATE_ROLES).account)
    }

    @Test
    fun `permission check denies missing permission`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId, ownerMemberId = member.id)
        val provider = provider(user, account, member)

        assertThrows<AccountPermissionDeniedException> {
            provider.requirePermission(accountId, RolePermission.CREATE_ROLES)
        }
    }

    @Test
    fun `update context uses locked repository lookup`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId, ownerMemberId = member.id)
        val accountRepository = InMemoryAccountRepository(listOf(account))
        authenticate(user)
        val provider =
            activeAccountContextProvider(
                InMemoryUserRepository(listOf(user)),
                accountRepository,
                InMemoryAccountMemberRepository(listOf(member)),
            )

        val context = provider.requireActiveContextForUpdate(accountId)

        assertEquals(account, context.account)
        assertTrue(accountRepository.findForUpdateEnabled)
    }

    private fun provider(
        user: com.mnemos.accountservice.domain.model.user.User,
        account: com.mnemos.accountservice.domain.model.account.Account,
        member: com.mnemos.accountservice.domain.model.account.AccountMember,
    ): com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider {
        authenticate(user)
        return activeAccountContextProvider(
            InMemoryUserRepository(listOf(user)),
            InMemoryAccountRepository(listOf(account)),
            InMemoryAccountMemberRepository(listOf(member)),
        )
    }
}
