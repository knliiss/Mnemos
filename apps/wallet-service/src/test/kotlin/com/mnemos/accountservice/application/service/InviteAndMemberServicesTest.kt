package com.mnemos.accountservice.application.service

import com.mnemos.accountservice.application.port.invite.InviteCodeGenerator
import com.mnemos.accountservice.application.query.account.invite.AccountInviteListItem
import com.mnemos.accountservice.application.query.account.invite.AccountInviteQueryRepository
import com.mnemos.accountservice.application.query.account.invite.AccountInviteStatus
import com.mnemos.accountservice.application.query.account.member.AccountMemberListItem
import com.mnemos.accountservice.application.query.account.member.AccountMemberQueryRepository
import com.mnemos.accountservice.application.service.invite.AccountInviteQueryService
import com.mnemos.accountservice.application.service.invite.CreateAccountInviteService
import com.mnemos.accountservice.application.service.invite.RevokeAccountInviteService
import com.mnemos.accountservice.application.service.member.AccountMemberQueryService
import com.mnemos.accountservice.application.service.member.JoinAccountService
import com.mnemos.accountservice.application.service.member.KickAccountMemberService
import com.mnemos.accountservice.domain.exception.account.member.AccountMemberAlreadyExistsException
import com.mnemos.accountservice.domain.exception.account.member.AccountMemberLimitReachedException
import com.mnemos.accountservice.domain.exception.invite.AccountInviteNotFoundException
import com.mnemos.accountservice.domain.exception.invite.AccountInviteUnavailableException
import com.mnemos.accountservice.domain.model.account.AccountStatus
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignmentActor
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignmentReason
import com.mnemos.accountservice.support.InMemoryAccountMemberRepository
import com.mnemos.accountservice.support.InMemoryAccountRepository
import com.mnemos.accountservice.support.InMemoryInviteRepository
import com.mnemos.accountservice.support.InMemoryRoleAssignmentRepository
import com.mnemos.accountservice.support.InMemoryRoleRepository
import com.mnemos.accountservice.support.InMemoryUserRepository
import com.mnemos.accountservice.support.SecurityContextTest
import com.mnemos.accountservice.support.TEST_TIME
import com.mnemos.accountservice.support.account
import com.mnemos.accountservice.support.activeAccountContextProvider
import com.mnemos.accountservice.support.authenticate
import com.mnemos.accountservice.support.currentUserProvider
import com.mnemos.accountservice.support.invite
import com.mnemos.accountservice.support.member
import com.mnemos.accountservice.support.role
import com.mnemos.accountservice.support.roleAccessGuard
import com.mnemos.accountservice.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import java.util.UUID

class InviteAndMemberServicesTest : SecurityContextTest() {
    @Test
    fun `create invite rejects zero and negative duration before accessing context`() {
        val context = emptyContext()
        val service = CreateAccountInviteService(context.provider, InMemoryInviteRepository(), fixedCodeGenerator())

        assertThrows<IllegalArgumentException> { service.createInvite(context.accountId, Duration.ZERO) }
        assertThrows<IllegalArgumentException> { service.createInvite(context.accountId, Duration.ofSeconds(-1)) }
    }

    @Test
    fun `create invite persists creator and expiration`() {
        val context = permittedContext(RolePermission.MANAGE_ACCOUNT_INVITES)
        val repository = InMemoryInviteRepository()
        val service = CreateAccountInviteService(context.provider, repository, fixedCodeGenerator())

        val result = service.createInvite(context.accountId, Duration.ofHours(2))

        val created = repository.invites.getValue(result.id)
        assertEquals(context.user.id, created.createdByUserId)
        assertEquals(FIXED_CODE, result.code)
        assertTrue(result.expiresAt!!.isAfter(result.createdAt))
        assertEquals(1, repository.createCount)
    }

    @Test
    fun `create invite without duration never expires`() {
        val context = permittedContext(RolePermission.MANAGE_ACCOUNT_INVITES)
        val result =
            CreateAccountInviteService(context.provider, InMemoryInviteRepository(), fixedCodeGenerator())
                .createInvite(context.accountId, null)

        assertNull(result.expiresAt)
    }

    @Test
    fun `revoke invite requires matching account invite`() {
        val context = permittedContext(RolePermission.MANAGE_ACCOUNT_INVITES)

        assertThrows<AccountInviteNotFoundException> {
            RevokeAccountInviteService(context.provider, InMemoryInviteRepository())
                .revokeInvite(context.accountId, InviteId(UUID.randomUUID()))
        }
    }

    @Test
    fun `revoke invite persists first change and ignores repeated revoke`() {
        val context = permittedContext(RolePermission.MANAGE_ACCOUNT_INVITES)
        val invite = invite(accountId = context.accountId, createdByUserId = context.user.id)
        val repository = InMemoryInviteRepository(listOf(invite))
        val service = RevokeAccountInviteService(context.provider, repository)

        service.revokeInvite(context.accountId, invite.id)
        service.revokeInvite(context.accountId, invite.id)

        assertEquals(1, repository.updateCount)
        assertTrue(invite.revokedAt != null)
    }

    @Test
    fun `invite query validates permission and returns repository data`() {
        val context = permittedContext(RolePermission.MANAGE_ACCOUNT_INVITES)
        val item =
            AccountInviteListItem(
                InviteId(UUID.randomUUID()),
                FIXED_CODE,
                context.user.id,
                TEST_TIME,
                null,
                null,
                null,
                AccountInviteStatus.ACTIVE,
            )
        var receivedAccountId: AccountId? = null
        var receivedNow: Instant? = null
        val repository =
            object : AccountInviteQueryRepository {
                override fun findAllByAccountId(
                    accountId: AccountId,
                    now: Instant,
                ): List<AccountInviteListItem> {
                    receivedAccountId = accountId
                    receivedNow = now
                    return listOf(item)
                }
            }

        assertEquals(listOf(item), AccountInviteQueryService(context.provider, repository).getInvites(context.accountId))
        assertEquals(context.accountId, receivedAccountId)
        assertTrue(receivedNow != null)
    }

    @Test
    fun `join maps malformed code to unavailable invite`() {
        val user = user()
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertThrows<AccountInviteUnavailableException> {
            joinService(users).joinAccount("not-a-valid-code")
        }
    }

    @Test
    fun `join rejects unknown invite and missing account`() {
        val user = user()
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)
        assertThrows<AccountInviteUnavailableException> { joinService(users).joinAccount(FIXED_CODE.value) }

        val invite = invite()
        assertThrows<AccountInviteUnavailableException> {
            joinService(users, invites = InMemoryInviteRepository(listOf(invite)))
                .joinAccount(FIXED_CODE.value)
        }
    }

    @Test
    fun `join rejects non-active account and vanished locked invite`() {
        val user = user()
        val users = InMemoryUserRepository(listOf(user))
        val deleting =
            account(
                status = AccountStatus.DELETING,
                deletionRequestedAt = TEST_TIME,
            )
        val invite = invite(accountId = deleting.id)
        authenticate(user)

        assertThrows<AccountInviteUnavailableException> {
            joinService(
                users,
                InMemoryAccountRepository(listOf(deleting)),
                invites = InMemoryInviteRepository(listOf(invite)),
            ).joinAccount(FIXED_CODE.value)
        }

        val active = account(id = invite.accountId)
        val inviteRepository = InMemoryInviteRepository(listOf(invite)).also { it.lockLookupEnabled = false }
        assertThrows<AccountInviteUnavailableException> {
            joinService(users, InMemoryAccountRepository(listOf(active)), invites = inviteRepository)
                .joinAccount(FIXED_CODE.value)
        }
    }

    @Test
    fun `join rejects existing membership before consuming invite`() {
        val account = account()
        val user = user()
        val existingMember = member(accountId = account.id, userId = user.id)
        val invite = invite(accountId = account.id)
        val users = InMemoryUserRepository(listOf(user))
        val invites = InMemoryInviteRepository(listOf(invite))
        authenticate(user)

        assertThrows<AccountMemberAlreadyExistsException> {
            joinService(
                users,
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(listOf(existingMember)),
                invites,
            ).joinAccount(FIXED_CODE.value)
        }
        assertNull(invite.consumedAt)
        assertEquals(0, invites.updateCount)
    }

    @Test
    fun `join rejects member limit before consuming invite`() {
        val account = account(maxMembers = 1)
        val existingMember = member(accountId = account.id)
        val user = user()
        val invite = invite(accountId = account.id)
        val users = InMemoryUserRepository(listOf(user))
        val invites = InMemoryInviteRepository(listOf(invite))
        authenticate(user)

        assertThrows<AccountMemberLimitReachedException> {
            joinService(
                users,
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(listOf(existingMember)),
                invites,
            ).joinAccount(FIXED_CODE.value)
        }
        assertNull(invite.consumedAt)
    }

    @Test
    fun `join consumes invite creates member and activates account`() {
        val account = account()
        val user = user()
        val invite = invite(accountId = account.id)
        val users = InMemoryUserRepository(listOf(user))
        val members = InMemoryAccountMemberRepository()
        val invites = InMemoryInviteRepository(listOf(invite))
        authenticate(user)

        val result =
            joinService(
                users,
                InMemoryAccountRepository(listOf(account)),
                members,
                invites,
            ).joinAccount(FIXED_CODE.value)

        assertEquals(account.id, result.accountId)
        assertEquals(user.id, members.members.getValue(result.memberId).userId)
        assertTrue(invite.consumedAt != null)
        assertEquals(account.id, user.activeAccountId)
        assertEquals(1, invites.updateCount)
        assertEquals(1, users.updateCount)
    }

    @Test
    fun `join assigns configured base role as system`() {
        val baseRole = role()
        val account = account(id = baseRole.accountId, baseRoleId = baseRole.id)
        val user = user()
        val invite = invite(accountId = account.id)
        val users = InMemoryUserRepository(listOf(user))
        val assignments = InMemoryRoleAssignmentRepository()
        authenticate(user)

        val result =
            joinService(
                users,
                InMemoryAccountRepository(listOf(account)),
                invites = InMemoryInviteRepository(listOf(invite)),
                assignments = assignments,
            ).joinAccount(FIXED_CODE.value)

        val assignment = assignments.find(baseRole.id, result.memberId)!!
        assertEquals(RoleAssignmentActor.System, assignment.assignedBy)
        assertEquals(RoleAssignmentReason.BASE_ROLE, assignment.reason)
    }

    @Test
    fun `kick clears target active account and deletes membership`() {
        val context = permittedContext(RolePermission.KICK_MEMBERS, owner = true)
        val targetUser = user(activeAccountId = context.accountId)
        val targetMember = member(accountId = context.accountId, userId = targetUser.id)
        context.users.users[targetUser.id] = targetUser
        context.members.members[targetMember.id] = targetMember
        val roles = InMemoryRoleRepository()
        val service =
            KickAccountMemberService(
                context.provider,
                roleAccessGuard(roles, context.members),
                context.members,
                context.users,
            )

        service.kickMember(context.accountId, targetMember.id)

        assertNull(targetUser.activeAccountId)
        assertFalse(context.members.members.containsKey(targetMember.id))
        assertEquals(
            targetUser.id,
            context.users.clearIfMatchesCalls
                .single()
                .first,
        )
    }

    @Test
    fun `member query validates active context and returns list`() {
        val context = permittedContext(RolePermission.VIEW_MEMBERS)
        val item = AccountMemberListItem(context.member.id, "Actor", false, null, TEST_TIME)
        var calledWith: AccountId? = null
        val repository =
            object : AccountMemberQueryRepository {
                override fun findAllByAccountId(accountId: AccountId): List<AccountMemberListItem> {
                    calledWith = accountId
                    return listOf(item)
                }
            }

        assertEquals(listOf(item), AccountMemberQueryService(context.provider, repository).getMembers(context.accountId))
        assertEquals(context.accountId, calledWith)
    }

    private data class Context(
        val accountId: AccountId,
        val user: com.mnemos.accountservice.domain.model.user.User,
        val member: com.mnemos.accountservice.domain.model.account.AccountMember,
        val users: InMemoryUserRepository,
        val members: InMemoryAccountMemberRepository,
        val provider: com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider,
    )

    private fun permittedContext(
        permission: RolePermission,
        owner: Boolean = false,
    ): Context {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val ownerMemberId =
            if (owner) {
                member.id
            } else {
                com.mnemos.accountservice.domain.model.account
                    .AccountMemberId(UUID.randomUUID())
            }
        val account = account(id = accountId, ownerMemberId = ownerMemberId)
        val users = InMemoryUserRepository(listOf(user))
        val accounts = InMemoryAccountRepository(listOf(account))
        val members = InMemoryAccountMemberRepository(listOf(member))
        members.permissions[member.id] = mutableSetOf(permission)
        authenticate(user)
        return Context(accountId, user, member, users, members, activeAccountContextProvider(users, accounts, members))
    }

    private fun emptyContext(): Context {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val users = InMemoryUserRepository(listOf(user))
        val members = InMemoryAccountMemberRepository()
        authenticate(user)
        return Context(
            accountId,
            user,
            member(accountId = accountId, userId = user.id),
            users,
            members,
            activeAccountContextProvider(users, InMemoryAccountRepository(), members),
        )
    }

    private fun joinService(
        users: InMemoryUserRepository,
        accounts: InMemoryAccountRepository = InMemoryAccountRepository(),
        members: InMemoryAccountMemberRepository = InMemoryAccountMemberRepository(),
        invites: InMemoryInviteRepository = InMemoryInviteRepository(),
        assignments: InMemoryRoleAssignmentRepository = InMemoryRoleAssignmentRepository(),
    ) = JoinAccountService(
        currentUserProvider(users),
        accounts,
        members,
        invites,
        users,
        assignments,
    )

    private fun fixedCodeGenerator() =
        object : InviteCodeGenerator {
            override fun generate(): InviteCode = FIXED_CODE
        }

    companion object {
        private val FIXED_CODE = InviteCode.of("ABCD-EFGH-JKLM-NPQR")
    }
}
