package com.mnemos.accountservice.support

import com.mnemos.accountservice.domain.exception.role.RoleAlreadyAssignedException
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.domain.model.account.invite.AccountInvite
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.invite.AccountInviteRepository
import com.mnemos.accountservice.domain.repository.role.RoleAssignmentRepository
import com.mnemos.accountservice.domain.repository.role.RoleRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.core.model.user.Username
import java.time.Instant

class InMemoryUserRepository(
    initial: Collection<User> = emptyList(),
) : UserRepository {
    val users = initial.associateByTo(linkedMapOf(), User::id)
    var createCount = 0
    var updateCount = 0
    val clearedAccountIds = mutableListOf<AccountId>()
    val clearIfMatchesCalls = mutableListOf<Triple<UserId, AccountId, Instant>>()

    override fun create(user: User): User {
        createCount++
        users[user.id] = user
        return user
    }

    override fun update(user: User): User {
        updateCount++
        users[user.id] = user
        return user
    }

    override fun findByTelegramId(telegramId: TelegramId): User? = users.values.firstOrNull { it.telegramId == telegramId }

    override fun findByKeycloakId(keycloakId: KeycloakId): User? = users.values.firstOrNull { it.keycloakId == keycloakId }

    override fun existsByUsername(username: Username): Boolean =
        users.values.any { it.username.value.equals(username.value, ignoreCase = true) }

    override fun existsByTelegramId(telegramId: TelegramId): Boolean = users.values.any { it.telegramId == telegramId }

    override fun clearActiveAccountByAccountId(
        accountId: AccountId,
        now: Instant,
    ) {
        clearedAccountIds += accountId
        users.values.filter { it.activeAccountId == accountId }.forEach {
            it.clearActiveAccount()
            users[it.id] = it
        }
    }

    override fun clearActiveAccountIfMatches(
        userId: UserId,
        accountId: AccountId,
        updatedAt: Instant,
    ): Boolean {
        clearIfMatchesCalls += Triple(userId, accountId, updatedAt)
        val user = users[userId] ?: return false
        if (user.activeAccountId != accountId) return false
        user.clearActiveAccount()
        users[user.id] = user
        return true
    }
}

class InMemoryAccountRepository(
    initial: Collection<Account> = emptyList(),
) : AccountRepository {
    val accounts = initial.associateByTo(linkedMapOf(), Account::id)
    var createCount = 0
    var updateCount = 0
    val lockCalls = mutableListOf<AccountId>()
    var lockResultOverride: Boolean? = null
    var findForUpdateEnabled = true

    override fun create(account: Account): Account {
        createCount++
        accounts[account.id] = account
        return account
    }

    override fun update(account: Account): Account {
        updateCount++
        accounts[account.id] = account
        return account
    }

    override fun findActiveById(accountId: AccountId): Account? = accounts[accountId]?.takeIf(Account::isActive)

    override fun existsByCristalixUuid(cristalixUuid: CristalixUuid): Boolean = accounts.values.any { it.cristalixUuid == cristalixUuid }

    override fun findAllAvailableForUser(userId: UserId): List<Account> = accounts.values.filter(Account::isActive)

    override fun lockById(id: AccountId): Boolean {
        lockCalls += id
        return lockResultOverride ?: accounts.containsKey(id)
    }

    override fun findByIdForUpdate(accountId: AccountId): Account? = if (findForUpdateEnabled) accounts[accountId] else null

    override fun findByIdIncludingInactive(accountId: AccountId): Account? = accounts[accountId]
}

class InMemoryAccountMemberRepository(
    initial: Collection<AccountMember> = emptyList(),
) : AccountMemberRepository {
    val members = initial.associateByTo(linkedMapOf(), AccountMember::id)
    val permissions = mutableMapOf<AccountMemberId, MutableSet<RolePermission>>()
    var createCount = 0
    val deletedIds = mutableListOf<AccountMemberId>()

    override fun create(member: AccountMember): AccountMember {
        createCount++
        members[member.id] = member
        return member
    }

    override fun findByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): AccountMember? = members.values.firstOrNull { it.accountId == accountId && it.userId == userId }

    override fun existsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): Boolean = findByAccountIdAndUserId(accountId, userId) != null

    override fun countByAccountId(accountId: AccountId): Int = members.values.count { it.accountId == accountId }

    override fun hasEffectivePermission(
        permission: RolePermission,
        memberId: AccountMemberId,
    ): Boolean = permission in permissions[memberId].orEmpty()

    override fun deleteById(id: AccountMemberId) {
        deletedIds += id
        members.remove(id)
    }

    override fun findByIdAndAccountId(
        memberId: AccountMemberId,
        accountId: AccountId,
    ): AccountMember? = members[memberId]?.takeIf { it.accountId == accountId }

    override fun findPermissions(memberId: AccountMemberId): Set<RolePermission> = permissions[memberId].orEmpty().toSet()

    override fun hasPermission(
        memberId: AccountMemberId,
        permission: RolePermission,
    ): Boolean = permission in permissions[memberId].orEmpty()
}

class InMemoryInviteRepository(
    initial: Collection<AccountInvite> = emptyList(),
) : AccountInviteRepository {
    val invites = initial.associateByTo(linkedMapOf(), AccountInvite::id)
    var createCount = 0
    var updateCount = 0
    var lockLookupEnabled = true

    override fun create(invite: AccountInvite): AccountInvite {
        createCount++
        invites[invite.id] = invite
        return invite
    }

    override fun update(invite: AccountInvite): AccountInvite {
        updateCount++
        invites[invite.id] = invite
        return invite
    }

    override fun findByCode(code: InviteCode): AccountInvite? = invites.values.firstOrNull { it.code == code }

    override fun findByIdAndAccountIdForUpdate(
        inviteId: InviteId,
        accountId: AccountId,
    ): AccountInvite? = if (lockLookupEnabled) invites[inviteId]?.takeIf { it.accountId == accountId } else null

    override fun findAllByAccountId(accountId: AccountId): List<AccountInvite> = invites.values.filter { it.accountId == accountId }
}

class InMemoryRoleRepository(
    initial: Collection<Role> = emptyList(),
) : RoleRepository {
    val roles = initial.associateByTo(linkedMapOf(), Role::id)
    var createCount = 0
    var updateCount = 0
    var updateAllCount = 0
    val deletedIds = mutableListOf<RoleId>()

    override fun create(role: Role): Role {
        createCount++
        roles[role.id] = role
        return role
    }

    override fun update(role: Role): Role {
        updateCount++
        roles[role.id] = role
        return role
    }

    override fun deleteById(id: RoleId) {
        deletedIds += id
        roles.remove(id)
    }

    override fun findById(id: RoleId): Role? = roles[id]

    override fun existsByAccountIdAndRoleName(
        accountId: AccountId,
        name: AccountRoleName,
    ): Boolean = roles.values.any { it.accountId == accountId && it.roleName == name }

    override fun findByIdAndAccountId(
        roleId: RoleId,
        accountId: AccountId,
    ): Role? = roles[roleId]?.takeIf { it.accountId == accountId }

    override fun findMinRank(accountId: AccountId): Long =
        roles.values.filter { it.accountId == accountId }.minOfOrNull(Role::hierarchyRank) ?: 0L

    override fun findMaxRank(accountId: AccountId): Long =
        roles.values.filter { it.accountId == accountId }.maxOfOrNull(Role::hierarchyRank) ?: 0L

    override fun findAllByAccountIdOrderByRank(accountId: AccountId): List<Role> =
        roles.values.filter { it.accountId == accountId }.sortedBy(Role::hierarchyRank)

    override fun updateAll(roles: Collection<Role>): List<Role> {
        updateAllCount++
        roles.forEach { this.roles[it.id] = it }
        return roles.toList()
    }

    override fun findAllByMemberIdAndAccountIdOrderByRank(
        memberId: AccountMemberId,
        accountId: AccountId,
    ): List<Role> = findAllByMemberId(accountId, memberId)

    override fun findAllByAccountId(accountId: AccountId): List<Role> =
        roles.values.filter { it.accountId == accountId }.sortedBy(Role::hierarchyRank)

    var memberRoles: MutableMap<AccountMemberId, MutableSet<RoleId>> = mutableMapOf()

    override fun findAllByMemberId(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): List<Role> =
        memberRoles[memberId]
            .orEmpty()
            .mapNotNull(roles::get)
            .filter { it.accountId == accountId }
            .sortedBy(Role::hierarchyRank)
}

class InMemoryRoleAssignmentRepository(
    initial: Collection<RoleAssignment> = emptyList(),
) : RoleAssignmentRepository {
    private val assignments = linkedMapOf<Pair<AccountMemberId, RoleId>, RoleAssignment>()
    var createCount = 0
    var removeCount = 0

    init {
        initial.forEach { assignments[it.accountMemberId to it.roleId] = it }
    }

    override fun create(assignment: RoleAssignment): RoleAssignment {
        val key = assignment.accountMemberId to assignment.roleId
        if (assignments.containsKey(key)) {
            throw RoleAlreadyAssignedException(assignment.accountMemberId, assignment.roleId)
        }
        createCount++
        assignments[key] = assignment
        return assignment
    }

    override fun remove(
        roleId: RoleId,
        accountMemberId: AccountMemberId,
    ): Boolean {
        removeCount++
        return assignments.remove(accountMemberId to roleId) != null
    }

    override fun find(
        roleId: RoleId,
        accountMemberId: AccountMemberId,
    ): RoleAssignment? = assignments[accountMemberId to roleId]

    override fun findAllByMemberId(accountMemberId: AccountMemberId): List<RoleAssignment> =
        assignments.values.filter { it.accountMemberId == accountMemberId }

    override fun findAllByRoleId(roleId: RoleId): List<RoleAssignment> = assignments.values.filter { it.roleId == roleId }

    override fun countByRoleId(roleId: RoleId): Int = findAllByRoleId(roleId).size

    override fun findMaxRankByMemberId(accountMemberId: AccountMemberId): Long = Long.MAX_VALUE

    override fun findMinRankByMemberId(accountMemberId: AccountMemberId): Long = Long.MAX_VALUE

    override fun exists(
        accountMemberId: AccountMemberId,
        roleId: RoleId,
    ): Boolean = assignments.containsKey(accountMemberId to roleId)
}
