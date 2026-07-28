package com.mnemos.account.application.service

import com.mnemos.account.application.service.permission.AccountPermissionQueryService
import com.mnemos.account.support.InMemoryAccountMemberRepository
import com.mnemos.account.support.InMemoryAccountRepository
import com.mnemos.account.support.account
import com.mnemos.account.support.member
import com.mnemos.account.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.collections.emptySet

class AccountPermissionQueryServiceTest {
    @Test
    fun `missing or inactive account has no permissions`() {
        val user = user()
        val service = AccountPermissionQueryService(InMemoryAccountRepository(), InMemoryAccountMemberRepository())

        assertFalse(service.hasPermission(AccountId(UUID.randomUUID()), user.id, RolePermission.VIEW_BETS))
        assertEquals(emptySet<RolePermission>(), service.getEffectivePermissions(AccountId(UUID.randomUUID()), user.id))
    }

    @Test
    fun `non-member has no permissions`() {
        val account = account()
        val user = user()
        val service =
            AccountPermissionQueryService(
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(),
            )

        assertFalse(service.hasPermission(account.id, user.id, RolePermission.VIEW_BETS))
        assertEquals(emptySet<RolePermission>(), service.getEffectivePermissions(account.id, user.id))
    }

    @Test
    fun `owner has every permission regardless of assignments`() {
        val user = user()
        val member = member(userId = user.id)
        val account = account(id = member.accountId, ownerMemberId = member.id)
        val service =
            AccountPermissionQueryService(
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(listOf(member)),
            )

        assertTrue(service.hasPermission(account.id, user.id, RolePermission.DELETE_ROLES))
        assertEquals(RolePermission.entries.toSet(), service.getEffectivePermissions(account.id, user.id))
    }

    @Test
    fun `member permission is resolved from assigned roles`() {
        val user = user()
        val member = member(userId = user.id)
        val account = account(id = member.accountId)
        val members = InMemoryAccountMemberRepository(listOf(member))
        members.permissions[member.id] = mutableSetOf(RolePermission.VIEW_BETS, RolePermission.PLACE_BETS)
        val service = AccountPermissionQueryService(InMemoryAccountRepository(listOf(account)), members)

        assertTrue(service.hasPermission(account.id, user.id, RolePermission.VIEW_BETS))
        assertFalse(service.hasPermission(account.id, user.id, RolePermission.DELETE_ROLES))
        assertEquals(setOf(RolePermission.VIEW_BETS, RolePermission.PLACE_BETS), service.getEffectivePermissions(account.id, user.id))
    }
}
