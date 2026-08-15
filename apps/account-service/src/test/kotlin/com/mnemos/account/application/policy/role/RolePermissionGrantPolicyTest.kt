package com.mnemos.account.application.policy.role

import com.mnemos.account.domain.exception.role.RoleAccessDeniedException
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RolePermissionGrantPolicyTest {
    private val policy = RolePermissionGrantPolicy()

    @Test
    fun `non owner cannot grant permission they do not have`() {
        val accountId = AccountId.generate()

        assertThrows(RoleAccessDeniedException::class.java) {
            policy.requireAllowed(
                accountId = accountId,
                isOwner = false,
                actorPermissions = setOf(RolePermission.ASSIGN_MEMBER_ROLES),
                requestedPermissions =
                    setOf(
                        RolePermission.ASSIGN_MEMBER_ROLES,
                        RolePermission.SPEND_WALLET_FUNDS,
                    ),
            )
        }
    }

    @Test
    fun `non owner can grant only their own effective permissions`() {
        val permissions =
            setOf(
                RolePermission.ASSIGN_MEMBER_ROLES,
                RolePermission.VIEW_BETS,
            )

        assertDoesNotThrow {
            policy.requireAllowed(
                accountId = AccountId.generate(),
                isOwner = false,
                actorPermissions = permissions,
                requestedPermissions = permissions,
            )
        }
    }

    @Test
    fun `owner can grant any permission`() {
        assertDoesNotThrow {
            policy.requireAllowed(
                accountId = AccountId.generate(),
                isOwner = true,
                actorPermissions = emptySet(),
                requestedPermissions = RolePermission.entries,
            )
        }
    }
}
