package com.mnemos.account.application.service.role

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentActor
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentReason
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class AdminRoleManagementServiceTest {
    @Test
    fun `admin creates role without account membership context`() {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(roleRepository.findAllByAccountIdOrderByRank(account.id)).thenReturn(emptyList())
        whenever(roleRepository.create(any())).thenAnswer { invocation -> invocation.getArgument<Role>(0) }

        val service =
            createService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                roleAssignmentRepository = roleAssignmentRepository,
            )

        val created =
            service.createRole(
                accountId = account.id,
                name = "Moderator",
                color = "#44AA88",
                permissions = setOf(RolePermission.MANAGE_MEMBERS),
            )

        assertEquals("Moderator", created.name)
        assertEquals(Role.RANK_STEP, created.hierarchyRank)
        assertTrue(created.permissions.contains(RolePermission.MANAGE_MEMBERS))

        verify(roleRepository).create(any())
    }

    @Test
    fun `admin updates role permissions without account membership context`() {
        val account = createAccount()
        val role = createRole(account.id)
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()

        whenever(roleRepository.findByIdAndAccountId(role.id, account.id)).thenReturn(role)
        whenever(roleRepository.update(role)).thenReturn(role)

        val service =
            createService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                roleAssignmentRepository = roleAssignmentRepository,
            )

        val updated =
            service.updatePermissions(
                accountId = account.id,
                roleId = role.id,
                permissions = mapOf(RolePermission.MANAGE_MEMBERS to false),
            )

        assertFalse(updated.permissions.contains(RolePermission.MANAGE_MEMBERS))
        verify(roleRepository).update(role)
    }

    @Test
    fun `admin reorders complete role hierarchy`() {
        val account = createAccount()
        val firstRole = createRole(account.id, hierarchyRank = Role.RANK_STEP, name = "First")
        val secondRole = createRole(account.id, hierarchyRank = Role.RANK_STEP * 2, name = "Second")
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(roleRepository.findAllByAccountIdOrderByRank(account.id)).thenReturn(listOf(firstRole, secondRole))
        whenever(roleRepository.updateAll(any())).thenAnswer { invocation -> invocation.getArgument<Collection<Role>>(0).toList() }

        val service =
            createService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                roleAssignmentRepository = roleAssignmentRepository,
            )

        val reordered =
            service.updateHierarchy(
                accountId = account.id,
                orderedRoleIds = listOf(secondRole.id, firstRole.id),
            )

        assertEquals(listOf(secondRole.id, firstRole.id), reordered.map { role -> role.id })
        assertEquals(Role.RANK_STEP, secondRole.hierarchyRank)
        assertEquals(Role.RANK_STEP * 2, firstRole.hierarchyRank)
        verify(roleRepository).updateAll(any())
    }

    @Test
    fun `deleting base role clears account base role before deleting role`() {
        val roleId = RoleId.generate()
        val account = createAccount(baseRoleId = roleId)
        val role = createRole(account.id, id = roleId)
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(roleRepository.findByIdAndAccountId(role.id, account.id)).thenReturn(role)
        whenever(accountRepository.update(account)).thenReturn(account)

        val service =
            createService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                roleAssignmentRepository = roleAssignmentRepository,
            )

        service.deleteRole(account.id, role.id)

        assertEquals(null, account.baseRoleId)
        verify(accountRepository).update(account)
        verify(roleRepository).deleteById(role.id)
    }

    @Test
    fun `admin assigns role as system actor without being an account member`() {
        val account = createAccount()
        val member =
            AccountMember.create(
                id = AccountMemberId.generate(),
                accountId = account.id,
                userId = UserId.generate(),
            )
        val role = createRole(account.id)
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()

        whenever(
            accountMemberRepository.findByIdAndAccountId(
                memberId = member.id,
                accountId = account.id,
            ),
        ).thenReturn(member)
        whenever(
            roleRepository.findByIdAndAccountId(
                roleId = role.id,
                accountId = account.id,
            ),
        ).thenReturn(role)
        whenever(roleAssignmentRepository.exists(member.id, role.id)).thenReturn(false)
        whenever(roleAssignmentRepository.create(any())).thenAnswer { invocation ->
            invocation.getArgument<RoleAssignment>(0)
        }

        val service =
            createService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                roleAssignmentRepository = roleAssignmentRepository,
            )

        service.assignRole(
            accountId = account.id,
            memberId = member.id,
            roleId = role.id,
        )

        val assignmentCaptor = argumentCaptor<RoleAssignment>()
        verify(roleAssignmentRepository).create(assignmentCaptor.capture())

        val assignment = assignmentCaptor.firstValue

        assertEquals(account.id, assignment.accountId)
        assertEquals(member.id, assignment.accountMemberId)
        assertEquals(role.id, assignment.roleId)
        assertEquals(RoleAssignmentActor.System, assignment.assignedBy)
        assertEquals(RoleAssignmentReason.MANUAL, assignment.reason)
    }

    @Test
    fun `admin revokes member role without account membership context`() {
        val account = createAccount()
        val member =
            AccountMember.create(
                id = AccountMemberId.generate(),
                accountId = account.id,
                userId = UserId.generate(),
            )
        val role = createRole(account.id)
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()

        whenever(accountMemberRepository.findByIdAndAccountId(member.id, account.id)).thenReturn(member)
        whenever(roleRepository.findByIdAndAccountId(role.id, account.id)).thenReturn(role)

        val service =
            createService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                roleAssignmentRepository = roleAssignmentRepository,
            )

        service.revokeRole(
            accountId = account.id,
            memberId = member.id,
            roleId = role.id,
        )

        verify(roleAssignmentRepository).remove(
            roleId = role.id,
            accountMemberId = member.id,
        )
    }

    private fun createService(
        accountRepository: AccountRepository,
        accountMemberRepository: AccountMemberRepository,
        roleRepository: RoleRepository,
        roleAssignmentRepository: RoleAssignmentRepository,
    ): AdminRoleManagementService =
        AdminRoleManagementService(
            accountRepository = accountRepository,
            accountMemberRepository = accountMemberRepository,
            roleRepository = roleRepository,
            roleAssignmentRepository = roleAssignmentRepository,
        )

    private fun createAccount(baseRoleId: RoleId? = null): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = AccountMemberId.generate(),
            cristalixUuid = CristalixUuid(UUID.randomUUID()),
            cristalixNickname = CristalixNickname("admin-role-test"),
            baseRoleId = baseRoleId,
        )

    private fun createRole(
        accountId: AccountId,
        id: RoleId = RoleId.generate(),
        hierarchyRank: Long = Role.RANK_STEP,
        name: String = "Moderator",
    ): Role =
        Role.restore(
            id = id,
            accountId = accountId,
            hierarchyRank = hierarchyRank,
            roleColor = HexColor("#44AA88"),
            roleName = AccountRoleName(name),
            permissions = mutableSetOf(RolePermission.MANAGE_MEMBERS),
        )
}
