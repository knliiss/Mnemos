package com.mnemos.telegram.bot.application.account.management

import com.mnemos.telegram.bot.application.account.management.model.AccountManagementConfirmation
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuFeedback
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class AccountManagementMenuContextTest {
    @Test
    fun `member selection replaces stale role and transient state`() {
        val memberId = UUID.randomUUID()
        val roleId = UUID.randomUUID()
        val initial =
            AccountManagementMenuContext.requestRoleDelete(
                AccountManagementMenuContext.unavailable(
                    AccountManagementMenuContext.showRole(emptyMap(), roleId),
                ),
            )

        val result = AccountManagementMenuContext.showMember(initial, memberId)

        assertEquals(AccountManagementMenuView.MEMBER, AccountManagementMenuContext.view(result))
        assertEquals(memberId, AccountManagementMenuContext.selectedMemberId(result))
        assertNull(AccountManagementMenuContext.selectedRoleId(result))
        assertNull(AccountManagementMenuContext.feedback(result))
        assertNull(AccountManagementMenuContext.confirmation(result))
    }

    @Test
    fun `updated feedback keeps current selection and clears confirmation`() {
        val memberId = UUID.randomUUID()
        val initial =
            AccountManagementMenuContext.requestKick(
                AccountManagementMenuContext.showMember(emptyMap(), memberId),
            )

        val result = AccountManagementMenuContext.updated(initial)

        assertEquals(AccountManagementMenuView.MEMBER, AccountManagementMenuContext.view(result))
        assertEquals(memberId, AccountManagementMenuContext.selectedMemberId(result))
        assertEquals(AccountManagementMenuFeedback.UPDATED, AccountManagementMenuContext.feedback(result))
        assertNull(AccountManagementMenuContext.confirmation(result))
    }

    @Test
    fun `malformed persisted context falls back safely`() {
        val context =
            mapOf(
                "account.management.view" to "BROKEN",
                "account.management.member-id" to "not-a-uuid",
                "account.management.role-id" to "also-broken",
                "account.management.feedback" to "UNKNOWN",
                "account.management.confirmation" to "UNKNOWN",
            )

        assertEquals(AccountManagementMenuView.OVERVIEW, AccountManagementMenuContext.view(context))
        assertNull(AccountManagementMenuContext.selectedMemberId(context))
        assertNull(AccountManagementMenuContext.selectedRoleId(context))
        assertNull(AccountManagementMenuContext.feedback(context))
        assertNull(AccountManagementMenuContext.confirmation(context))
    }

    @Test
    fun `confirmation state is explicit`() {
        val roleId = UUID.randomUUID()
        val initial = AccountManagementMenuContext.showRole(emptyMap(), roleId)

        val result = AccountManagementMenuContext.requestRoleDelete(initial)

        assertEquals(AccountManagementConfirmation.DELETE_ROLE, AccountManagementMenuContext.confirmation(result))
        assertEquals(roleId, AccountManagementMenuContext.selectedRoleId(result))
    }
}
