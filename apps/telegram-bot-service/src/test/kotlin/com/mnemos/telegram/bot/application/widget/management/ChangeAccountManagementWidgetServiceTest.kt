package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementSnapshot
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementRole
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetMutationGuard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class ChangeAccountManagementWidgetServiceTest {
    private val gateway = mock<AccountManagementGateway>()
    private val guard = mock<AccountManagementWidgetMutationGuard>()
    private val service = ChangeAccountManagementWidgetService(gateway, guard)
    private val roleId = UUID.randomUUID()
    private val memberId = UUID.randomUUID()
    private val accountId = UUID.randomUUID()

    @Test
    fun permissionToggleUsesOnlyDeclaredPermissionOrdinalAndAuthenticatedRole() {
        val view = view().copy(
            canManageRoles = true,
            roles = listOf(AccountManagementWidgetRole(
                roleId, "Officer", 1, 3, permissions = emptySet(), canEditPermissions = true,
            )),
        )
        whenever(guard.claimQuery(11L, "q")).thenReturn(true)
        whenever(gateway.setRolePermission(11L, roleId, TelegramAccountManagementPermission.MANAGE_ROLES, true))
            .thenReturn(TelegramAccountManagementMutationResult.UPDATED)

        assertEquals(
            AccountManagementWidgetMutationResult.APPLIED,
            service.setRolePermission(11L, "q", view, 0, 2, true),
        )
        verify(gateway).setRolePermission(11L, roleId, TelegramAccountManagementPermission.MANAGE_ROLES, true)
    }

    @Test
    fun invalidPermissionIndexAndOwnRoleCannotCallGateway() {
        val view = view().copy(canManageRoles = true)
        assertEquals(
            AccountManagementWidgetMutationResult.INVALID,
            service.setRolePermission(11L, "q", view, 0, 999, true),
        )
        assertEquals(
            AccountManagementWidgetMutationResult.FORBIDDEN,
            service.setRolePermission(11L, "q", view.copy(
                roles = listOf(AccountManagementWidgetRole(roleId, "Owner", 1, 2, canEditPermissions = false)),
            ), 0, 2, true),
        )
        verifyNoInteractions(guard, gateway)
    }

    @Test
    fun moveRoleRevalidatesWholeAccountOrderingAndPermission() {
        val secondId = UUID.randomUUID()
        val current = view().copy(canManageHierarchy = true)
        val snapshot = TelegramAccountManagementSnapshot(
            accountId = accountId,
            cristalixNickname = "Kirito",
            actorMemberId = UUID.randomUUID(),
            actorPermissions = setOf(TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY),
            members = emptyList(),
            roles = listOf(
                TelegramAccountManagementRole(roleId, "Officer", "#FFFFFF", 1, emptySet()),
                TelegramAccountManagementRole(secondId, "Default", "#AAAAAA", 2, emptySet()),
            ),
        )
        whenever(gateway.getManagement(11L)).thenReturn(TelegramAccountManagementResult.Available(snapshot))
        whenever(guard.claimQuery(11L, "q")).thenReturn(true)
        whenever(gateway.moveRole(11L, roleId, 1)).thenReturn(TelegramAccountManagementMutationResult.UPDATED)

        assertEquals(AccountManagementWidgetMutationResult.APPLIED, service.moveRole(11L, "q", current, 0, 1))
        verify(gateway).moveRole(11L, roleId, 1)
    }

    @Test
    fun moveRoleRejectsStaleHierarchyWithoutMutatingIt() {
        val current = view().copy(canManageHierarchy = true)
        whenever(gateway.getManagement(11L)).thenReturn(
            TelegramAccountManagementResult.Available(
                TelegramAccountManagementSnapshot(
                    accountId = accountId,
                    cristalixNickname = "Kirito",
                    actorMemberId = UUID.randomUUID(),
                    actorPermissions = setOf(TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY),
                    members = emptyList(),
                    roles = listOf(
                        TelegramAccountManagementRole(roleId, "Officer", "#FFFFFF", 8, emptySet()),
                    ),
                ),
            ),
        )

        assertEquals(AccountManagementWidgetMutationResult.CONFLICT, service.moveRole(11L, "q", current, 0, 1))
        verifyNoInteractions(guard)
    }

    @Test
    fun roleDeletionRequiresManagePermissionAndVisibleServerRole() {
        assertEquals(
            AccountManagementWidgetMutationResult.FORBIDDEN,
            service.deleteRole(11L, "q", view().copy(canManageRoles = false), 0),
        )
        assertEquals(
            AccountManagementWidgetMutationResult.CONFLICT,
            service.deleteRole(11L, "q", view().copy(canManageRoles = true), 3),
        )
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun roleDeletionCanExecuteOnlyOncePerQueryAndUsesDomainRoleId() {
        whenever(guard.claimQuery(11L, "q")).thenReturn(true, false)
        whenever(gateway.deleteRole(11L, roleId)).thenReturn(TelegramAccountManagementMutationResult.UPDATED)
        val current = view().copy(canManageRoles = true)
        assertEquals(AccountManagementWidgetMutationResult.APPLIED, service.deleteRole(11L, "q", current, 0))
        assertEquals(AccountManagementWidgetMutationResult.ALREADY, service.deleteRole(11L, "q", current, 0))
        verify(gateway).deleteRole(11L, roleId)
    }

    @Test
    fun cannotRemoveOwnerOrSelfEvenWithPermission() {
        val owner = view(member = member(owner = true))
        val self = view(member = member(self = true))
        assertEquals(AccountManagementWidgetMutationResult.FORBIDDEN, service.kickMember(11L, "q", owner, 0))
        assertEquals(AccountManagementWidgetMutationResult.FORBIDDEN, service.kickMember(11L, "q", self, 0))
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun cannotAssignRolesToSelfOrOwner() {
        assertEquals(
            AccountManagementWidgetMutationResult.FORBIDDEN,
            service.assignRole(11L, "q", view(member = member(self = true)), 0, 0, true),
        )
        assertEquals(
            AccountManagementWidgetMutationResult.FORBIDDEN,
            service.assignRole(11L, "q", view(member = member(owner = true)), 0, 0, true),
        )
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun accountOwnerCanAssignOwnRoleWhenAccountServicePolicyAllowsIt() {
        val current = view(member = member(owner = true, self = true)).copy(actorIsOwner = true)
        whenever(guard.claimQuery(11L, "q")).thenReturn(true)
        whenever(gateway.setMemberRole(11L, memberId, roleId, true))
            .thenReturn(TelegramAccountManagementMutationResult.UPDATED)

        assertEquals(
            AccountManagementWidgetMutationResult.APPLIED,
            service.assignRole(11L, "q", current, 0, 0, true),
        )
        verify(gateway).setMemberRole(11L, memberId, roleId, true)
    }

    @Test
    fun roleChangeRequiresExplicitPermission() {
        val blocked = view(canAssign = false)
        assertEquals(
            AccountManagementWidgetMutationResult.FORBIDDEN,
            service.assignRole(11L, "q", blocked, 0, 0, true),
        )
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun roleAssignmentCanOnlyTargetServerReturnedMemberAndRole() {
        assertEquals(
            AccountManagementWidgetMutationResult.CONFLICT,
            service.assignRole(11L, "q", view(), 5, 0, true),
        )
        assertEquals(
            AccountManagementWidgetMutationResult.CONFLICT,
            service.assignRole(11L, "q", view(), 0, 5, true),
        )
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun idempotentRoleSelectionDoesNotMutateBackend() {
        val current = view(member = member(roles = setOf(roleId)))
        assertEquals(
            AccountManagementWidgetMutationResult.APPLIED,
            service.assignRole(11L, "q", current, 0, 0, true),
        )
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun roleAssignmentUsesDomainGatewayAndUniqueQueryClaim() {
        whenever(guard.claimQuery(11L, "q")).thenReturn(true)
        whenever(gateway.setMemberRole(11L, memberId, roleId, true))
            .thenReturn(TelegramAccountManagementMutationResult.UPDATED)

        assertEquals(
            AccountManagementWidgetMutationResult.APPLIED,
            service.assignRole(11L, "q", view(), 0, 0, true),
        )
        verify(gateway).setMemberRole(11L, memberId, roleId, true)
        verify(guard).claimQuery(11L, "q")
    }

    @Test
    fun claimedQueryCannotBeExecutedTwice() {
        whenever(guard.claimQuery(11L, "q")).thenReturn(false)
        assertEquals(AccountManagementWidgetMutationResult.ALREADY, service.kickMember(11L, "q", view(), 0))
        verifyNoInteractions(gateway)
    }

    @Test
    fun successfulKickUsesOnlyServerReturnedMemberId() {
        whenever(guard.claimQuery(11L, "q")).thenReturn(true)
        whenever(gateway.kickMember(11L, memberId)).thenReturn(TelegramAccountManagementMutationResult.UPDATED)
        assertEquals(AccountManagementWidgetMutationResult.APPLIED, service.kickMember(11L, "q", view(), 0))
        verify(gateway).kickMember(11L, memberId)
    }

    private fun member(
        owner: Boolean = false,
        self: Boolean = false,
        roles: Set<UUID> = emptySet(),
    ) = AccountManagementWidgetMember(memberId, "Asuna", "Member", owner, self, roles)

    private fun view(
        member: AccountManagementWidgetMember = member(),
        canAssign: Boolean = true,
    ) = AccountManagementWidgetView(
        accountId = accountId,
        nickname = "Kirito",
        page = 0,
        pages = 1,
        totalMembers = 1,
        totalRoles = 1,
        members = listOf(member),
        roles = listOf(AccountManagementWidgetRole(roleId, "Officer", 1, 3)),
        canRemoveMembers = true,
        canAssignRoles = canAssign,
    )
}
