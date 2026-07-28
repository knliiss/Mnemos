package com.mnemos.account.application.service

import com.mnemos.account.api.dto.account.CreateAccountRequest
import com.mnemos.account.application.service.account.ChangeActiveAccountService
import com.mnemos.account.application.service.account.CreateAccountService
import com.mnemos.account.application.service.account.DeleteAccountService
import com.mnemos.account.application.service.account.LeaveActiveAccountService
import com.mnemos.account.application.service.account.TransferAccountOwnerShipService
import com.mnemos.account.application.service.account.UpdateAccountBaseRoleService
import com.mnemos.account.domain.exception.account.AccountIdentityConflictException
import com.mnemos.account.domain.exception.account.AccountNotActiveException
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.AccountOwnerCannotLeaveException
import com.mnemos.account.domain.exception.account.AccountOwnerRequiredException
import com.mnemos.account.domain.exception.account.ActiveAccountNotSetException
import com.mnemos.account.domain.exception.account.cristalix.CristalixPlayerNotFoundException
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.account.domain.exception.account.member.ActiveAccountMembershipNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.integration.cristalix.CristalixIdentityProvider
import com.mnemos.account.integration.cristalix.model.CristalixPlayer
import com.mnemos.account.support.InMemoryAccountMemberRepository
import com.mnemos.account.support.InMemoryAccountRepository
import com.mnemos.account.support.InMemoryRoleRepository
import com.mnemos.account.support.InMemoryUserRepository
import com.mnemos.account.support.RecordingOutboxEventWriter
import com.mnemos.account.support.SecurityContextTest
import com.mnemos.account.support.account
import com.mnemos.account.support.activeAccountContextProvider
import com.mnemos.account.support.authenticate
import com.mnemos.account.support.currentUserProvider
import com.mnemos.account.support.member
import com.mnemos.account.support.role
import com.mnemos.account.support.roleAccessGuard
import com.mnemos.account.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class AccountCommandServicesTest : SecurityContextTest() {
    @Test
    fun `create account resolves cristalix player and creates owner membership`() {
        val owner = user()
        val users = InMemoryUserRepository(listOf(owner))
        val accounts = InMemoryAccountRepository()
        val members = InMemoryAccountMemberRepository()
        val outbox = RecordingOutboxEventWriter()
        val canonical = CristalixPlayer(CristalixUuid(UUID.randomUUID()), CristalixNickname("CanonicalName"))
        authenticate(owner)
        val service =
            createService(
                users = users,
                accounts = accounts,
                members = members,
                outbox = outbox
            ) { canonical }

        val result = service.createAccount(CreateAccountRequest("requestedName"))

        val createdAccount = accounts.accounts.getValue(result.accountId)
        val createdMember = members.members.getValue(result.memberId)
        assertEquals(canonical.uuid, createdAccount.cristalixUuid)
        assertEquals(canonical.nickname, createdAccount.cristalixNickname)
        assertEquals(result.memberId, createdAccount.ownerMemberId)
        assertEquals(owner.id, createdMember.userId)
        assertEquals(result.accountId, createdMember.accountId)
        assertEquals(result.accountId, owner.activeAccountId)
        assertEquals(1, users.updateCount)

        val recordedEvent = outbox.events.single()

        assertEquals(
            "mnemos.account.events.v1",
            recordedEvent.destination.topic
        )

        assertEquals(
            result.accountId.value.toString(),
            recordedEvent.destination.key
        )

        assertEquals(
            "account.created",
            recordedEvent.event.eventType
        )

        assertEquals(
            1,
            recordedEvent.event.eventVersion
        )

        assertEquals(
            result.accountId.value,
            recordedEvent.event.aggregateId
        )

        assertEquals(
            owner.id.value,
            recordedEvent.metadata?.actorUserId
        )

        val payload =
            recordedEvent.event.payload as
                    com.mnemos.contracts.account.v1.AccountCreatedV1

        assertEquals(result.memberId.value, payload.ownerMemberId)
        assertEquals(owner.id.value, payload.ownerUserId)
        assertEquals(canonical.uuid.value, payload.cristalixUuid)
        assertEquals(canonical.nickname.value, payload.cristalixNickname)
        assertEquals(createdAccount.maxMembers, payload.maxMembers)
    }

    @Test
    fun `create account rejects unknown cristalix player before mutation`() {
        val owner = user()
        val users = InMemoryUserRepository(listOf(owner))
        val accounts = InMemoryAccountRepository()
        val members = InMemoryAccountMemberRepository()
        authenticate(owner)

        assertThrows<CristalixPlayerNotFoundException> {
            createService(users, accounts, members) { null }
                .createAccount(CreateAccountRequest("UnknownPlayer"))
        }
        assertEquals(0, accounts.createCount)
        assertEquals(0, members.createCount)
        assertEquals(0, users.updateCount)
    }

    @Test
    fun `create account rejects already linked cristalix identity`() {
        val owner = user()
        val existingAccount = account()
        val users = InMemoryUserRepository(listOf(owner))
        val accounts = InMemoryAccountRepository(listOf(existingAccount))
        val members = InMemoryAccountMemberRepository()
        val player = CristalixPlayer(existingAccount.cristalixUuid, existingAccount.cristalixNickname)
        authenticate(owner)

        assertThrows<AccountIdentityConflictException> {
            createService(users, accounts, members) { player }
                .createAccount(
                    CreateAccountRequest(player.nickname.value)
                )
        }
        assertEquals(0, accounts.createCount)
        assertEquals(0, members.createCount)
    }

    @Test
    fun `change active account requires membership`() {
        val user = user()
        val targetId = AccountId(UUID.randomUUID())
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)
        val service =
            ChangeActiveAccountService(
                users,
                InMemoryAccountRepository(),
                InMemoryAccountMemberRepository(),
                currentUserProvider(users),
            )

        assertThrows<AccountMemberNotFoundException> { service.changeActiveAccount(targetId) }
        assertNull(user.activeAccountId)
        assertEquals(0, users.updateCount)
    }

    @Test
    fun `change active account updates user only after membership validation`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user()
        val membership = member(accountId = accountId, userId = user.id)
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)
        val service =
            ChangeActiveAccountService(
                users,
                InMemoryAccountRepository(),
                InMemoryAccountMemberRepository(listOf(membership)),
                currentUserProvider(users),
            )

        service.changeActiveAccount(accountId)

        assertEquals(accountId, user.activeAccountId)
        assertEquals(1, users.updateCount)
    }

    @Test
    fun `delete account rejects non-active requested account`() {
        val activeId = AccountId(UUID.randomUUID())
        val requestedId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = activeId)
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)
        val service =
            DeleteAccountService(
                currentUserProvider(users),
                InMemoryAccountRepository(),
                InMemoryAccountMemberRepository(),
                users,
            )

        assertThrows<AccountNotActiveException> { service.deleteAccount(requestedId) }
    }

    @Test
    fun `delete account rejects missing account and missing membership`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertThrows<AccountNotFoundException> {
            DeleteAccountService(
                currentUserProvider(users),
                InMemoryAccountRepository(),
                InMemoryAccountMemberRepository(),
                users,
            ).deleteAccount(accountId)
        }

        val account = account(id = accountId)
        assertThrows<ActiveAccountMembershipNotFoundException> {
            DeleteAccountService(
                currentUserProvider(users),
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(),
                users,
            ).deleteAccount(accountId)
        }
    }

    @Test
    fun `delete account requires owner membership`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val actorMember = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId, ownerMemberId = AccountMemberId(UUID.randomUUID()))
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertThrows<AccountOwnerRequiredException> {
            DeleteAccountService(
                currentUserProvider(users),
                InMemoryAccountRepository(listOf(account)),
                InMemoryAccountMemberRepository(listOf(actorMember)),
                users,
            ).deleteAccount(accountId)
        }
    }

    @Test
    fun `delete account marks deleting and clears all active references`() {
        val accountId = AccountId(UUID.randomUUID())
        val owner = user(activeAccountId = accountId)
        val other = user(activeAccountId = accountId)
        val ownerMember = member(accountId = accountId, userId = owner.id)
        val account = account(id = accountId, ownerMemberId = ownerMember.id)
        val users = InMemoryUserRepository(listOf(owner, other))
        val accounts = InMemoryAccountRepository(listOf(account))
        authenticate(owner)

        DeleteAccountService(
            currentUserProvider(users),
            accounts,
            InMemoryAccountMemberRepository(listOf(ownerMember)),
            users,
        ).deleteAccount(accountId)

        assertEquals(AccountStatus.DELETING, account.status)
        assertNull(owner.activeAccountId)
        assertNull(other.activeAccountId)
        assertEquals(listOf(accountId), users.clearedAccountIds)
        assertEquals(1, accounts.updateCount)
    }

    @Test
    fun `leave account rejects user without active account`() {
        val user = user()
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)
        val contextProvider = activeAccountContextProvider(users, InMemoryAccountRepository(), InMemoryAccountMemberRepository())

        assertThrows<ActiveAccountNotSetException> {
            LeaveActiveAccountService(users, InMemoryAccountMemberRepository(), currentUserProvider(users), contextProvider)
                .leaveActiveAccount()
        }
    }

    @Test
    fun `leave account clears stale active account when account disappeared`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val users = InMemoryUserRepository(listOf(user))
        val members = InMemoryAccountMemberRepository()
        authenticate(user)
        val service =
            LeaveActiveAccountService(
                users,
                members,
                currentUserProvider(users),
                activeAccountContextProvider(users, InMemoryAccountRepository(), members),
            )

        assertThrows<AccountNotFoundException> { service.leaveActiveAccount() }
        assertNull(user.activeAccountId)
        assertEquals(1, users.updateCount)
    }

    @Test
    fun `owner cannot leave account`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId, ownerMemberId = member.id)
        val users = InMemoryUserRepository(listOf(user))
        val members = InMemoryAccountMemberRepository(listOf(member))
        authenticate(user)

        assertThrows<AccountOwnerCannotLeaveException> {
            LeaveActiveAccountService(
                users,
                members,
                currentUserProvider(users),
                activeAccountContextProvider(users, InMemoryAccountRepository(listOf(account)), members),
            ).leaveActiveAccount()
        }
        assertTrue(members.members.containsKey(member.id))
        assertEquals(accountId, user.activeAccountId)
    }

    @Test
    fun `non-owner leaves by deleting membership then clearing active account`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)
        val member = member(accountId = accountId, userId = user.id)
        val account = account(id = accountId)
        val users = InMemoryUserRepository(listOf(user))
        val members = InMemoryAccountMemberRepository(listOf(member))
        authenticate(user)

        LeaveActiveAccountService(
            users,
            members,
            currentUserProvider(users),
            activeAccountContextProvider(users, InMemoryAccountRepository(listOf(account)), members),
        ).leaveActiveAccount()

        assertFalse(members.members.containsKey(member.id))
        assertNull(user.activeAccountId)
        assertEquals(1, users.updateCount)
    }

    @Test
    fun `ownership transfer requires current owner`() {
        val accountId = AccountId(UUID.randomUUID())
        val actor = user(activeAccountId = accountId)
        val actorMember = member(accountId = accountId, userId = actor.id)
        val targetMember = member(accountId = accountId)
        val account = account(id = accountId, ownerMemberId = AccountMemberId(UUID.randomUUID()))
        val users = InMemoryUserRepository(listOf(actor))
        val members = InMemoryAccountMemberRepository(listOf(actorMember, targetMember))
        authenticate(actor)

        assertThrows<AccountOwnerRequiredException> {
            TransferAccountOwnerShipService(
                activeAccountContextProvider(users, InMemoryAccountRepository(listOf(account)), members),
                InMemoryAccountRepository(listOf(account)),
                members,
            ).transfer(targetMember.id, accountId)
        }
    }

    @Test
    fun `ownership transfer is idempotent for current owner`() {
        val accountId = AccountId(UUID.randomUUID())
        val owner = user(activeAccountId = accountId)
        val ownerMember = member(accountId = accountId, userId = owner.id)
        val account = account(id = accountId, ownerMemberId = ownerMember.id)
        val users = InMemoryUserRepository(listOf(owner))
        val members = InMemoryAccountMemberRepository(listOf(ownerMember))
        val accounts = InMemoryAccountRepository(listOf(account))
        authenticate(owner)

        TransferAccountOwnerShipService(
            activeAccountContextProvider(users, accounts, members),
            accounts,
            members,
        ).transfer(ownerMember.id, accountId)

        assertEquals(0, accounts.updateCount)
    }

    @Test
    fun `ownership transfer validates target belongs to account`() {
        val accountId = AccountId(UUID.randomUUID())
        val owner = user(activeAccountId = accountId)
        val ownerMember = member(accountId = accountId, userId = owner.id)
        val account = account(id = accountId, ownerMemberId = ownerMember.id)
        val users = InMemoryUserRepository(listOf(owner))
        val members = InMemoryAccountMemberRepository(listOf(ownerMember))
        val accounts = InMemoryAccountRepository(listOf(account))
        authenticate(owner)

        assertThrows<AccountMemberNotFoundException> {
            TransferAccountOwnerShipService(
                activeAccountContextProvider(users, accounts, members),
                accounts,
                members,
            ).transfer(AccountMemberId(UUID.randomUUID()), accountId)
        }
    }

    @Test
    fun `ownership transfer updates account`() {
        val accountId = AccountId(UUID.randomUUID())
        val owner = user(activeAccountId = accountId)
        val ownerMember = member(accountId = accountId, userId = owner.id)
        val targetMember = member(accountId = accountId)
        val account = account(id = accountId, ownerMemberId = ownerMember.id)
        val users = InMemoryUserRepository(listOf(owner))
        val members = InMemoryAccountMemberRepository(listOf(ownerMember, targetMember))
        val accounts = InMemoryAccountRepository(listOf(account))
        authenticate(owner)

        TransferAccountOwnerShipService(
            activeAccountContextProvider(users, accounts, members),
            accounts,
            members,
        ).transfer(targetMember.id, accountId)

        assertEquals(targetMember.id, account.ownerMemberId)
        assertEquals(1, accounts.updateCount)
    }

    @Test
    fun `base role update sets valid lower role and persists only changes`() {
        val accountId = AccountId(UUID.randomUUID())
        val owner = user(activeAccountId = accountId)
        val ownerMember = member(accountId = accountId, userId = owner.id)
        val account = account(id = accountId, ownerMemberId = ownerMember.id)
        val baseRole = role(accountId = accountId, rank = Role.RANK_STEP)
        val users = InMemoryUserRepository(listOf(owner))
        val accounts = InMemoryAccountRepository(listOf(account))
        val members = InMemoryAccountMemberRepository(listOf(ownerMember))
        members.permissions[ownerMember.id] = mutableSetOf(RolePermission.MANAGE_ROLES)
        val roles = InMemoryRoleRepository(listOf(baseRole))
        authenticate(owner)
        val service =
            UpdateAccountBaseRoleService(
                activeAccountContextProvider(users, accounts, members),
                accounts,
                roleAccessGuard(roles, members),
            )

        service.updateBaseRole(accountId, baseRole.id)
        service.updateBaseRole(accountId, baseRole.id)

        assertEquals(baseRole.id, account.baseRoleId)
        assertEquals(1, accounts.updateCount)
    }

    @Test
    fun `base role update rejects role outside account`() {
        val accountId = AccountId(UUID.randomUUID())
        val owner = user(activeAccountId = accountId)
        val ownerMember = member(accountId = accountId, userId = owner.id)
        val account = account(id = accountId, ownerMemberId = ownerMember.id)
        val foreignRole = role()
        val users = InMemoryUserRepository(listOf(owner))
        val accounts = InMemoryAccountRepository(listOf(account))
        val members = InMemoryAccountMemberRepository(listOf(ownerMember))
        members.permissions[ownerMember.id] = mutableSetOf(RolePermission.MANAGE_ROLES)
        authenticate(owner)

        assertThrows<com.mnemos.account.domain.exception.role.RoleNotFoundException> {
            UpdateAccountBaseRoleService(
                activeAccountContextProvider(users, accounts, members),
                accounts,
                roleAccessGuard(InMemoryRoleRepository(listOf(foreignRole)), members),
            ).updateBaseRole(accountId, foreignRole.id)
        }
    }

    private fun createService(
        users: InMemoryUserRepository,
        accounts: InMemoryAccountRepository,
        members: InMemoryAccountMemberRepository,
        outbox: RecordingOutboxEventWriter =
            RecordingOutboxEventWriter(),
        resolver: (CristalixNickname) -> CristalixPlayer?
    ): CreateAccountService {
        val transactionService =
            CreateAccountTransactionService(
                accountRepository = accounts,
                accountMemberRepository = members,
                userRepository = users,
                currentUserProvider = currentUserProvider(users),
                outboxEventWriter = outbox,
                eventMetadataProvider =
                    EventMetadataProvider {
                        EventMetadata(
                            correlationId = "test-request"
                        )
                    }
            )

        return CreateAccountService(
            cristalixIdentityProvider =
                object : CristalixIdentityProvider {
                    override fun findByNickname(
                        nickname: CristalixNickname
                    ): CristalixPlayer? = resolver(nickname)
                },
            transactionService = transactionService
        )
    }
}
