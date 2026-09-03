package com.mnemos.account.application.service.account

import com.mnemos.account.api.dto.account.AdminAccountDetailsResponse
import com.mnemos.account.api.dto.account.AdminAccountMemberResponse
import com.mnemos.account.application.event.account.AccountIntegrationEvents
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.member.AccountMemberAlreadyExistsException
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.account.domain.exception.account.member.AccountOwnerCannotBeRemovedException
import com.mnemos.account.domain.exception.user.UserIdNotFoundException
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AdminAccountManagementService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val roleAssignmentRepository: RoleAssignmentRepository,
    private val userRepository: UserRepository,
    private val integrationEventPublisher: IntegrationEventPublisher,
) {
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun getDetails(accountId: AccountId): AdminAccountDetailsResponse {
        val account =
            accountRepository.findByIdIncludingInactive(accountId)
                ?: throw AccountNotFoundException(accountId)

        val members = accountMemberRepository.findAllByAccountId(accountId)

        return account.toDetailsResponse(members)
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun addMember(
        accountId: AccountId,
        userId: UserId,
    ) {
        val account = requireAccountForUpdate(accountId)
        val user = requireUser(userId)

        if (accountMemberRepository.existsByAccountIdAndUserId(accountId, userId)) {
            throw AccountMemberAlreadyExistsException(accountId, userId)
        }

        val now = Instant.now()
        val member = createAdministrativeMember(account, user, now)

        publishMemberJoined(
            account = account,
            member = member,
            now = now,
        )
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun removeMember(
        accountId: AccountId,
        memberId: AccountMemberId,
    ) {
        val account = requireAccountForUpdate(accountId)
        val member = requireMember(accountId, memberId)

        if (account.isOwner(member.id)) {
            throw AccountOwnerCannotBeRemovedException(accountId)
        }

        val now = Instant.now()
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        userRepository.clearActiveAccountIfMatches(
            userId = member.userId,
            accountId = accountId,
            updatedAt = now,
        )
        accountMemberRepository.deleteById(member.id)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.memberRemoved(
                account = account,
                member = member,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun transferOwnership(
        accountId: AccountId,
        newOwnerUserId: UserId,
    ) {
        val account = requireAccountForUpdate(accountId)
        val newOwnerUser = requireUser(newOwnerUserId)
        val existingMember =
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = newOwnerUserId,
            )

        if (existingMember != null && account.isOwner(existingMember.id)) {
            return
        }

        val previousOwner = requireMember(accountId, account.ownerMemberId)
        val now = Instant.now()
        val newOwner = existingMember ?: createAdministrativeMember(account, newOwnerUser, now)

        if (existingMember == null) {
            publishMemberJoined(
                account = account,
                member = newOwner,
                now = now,
            )
        }

        if (!account.transferOwnership(newOwner.id, now)) {
            return
        }

        val updatedAccount = accountRepository.update(account)
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.ownershipTransferred(
                account = updatedAccount,
                previousOwner = previousOwner,
                newOwner = newOwner,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun deleteAccount(accountId: AccountId) {
        val account = requireAccountForUpdate(accountId)
        val now = Instant.now()
        val recipientUserIds = accountMemberRepository.findUserIdsByAccountId(accountId)

        account.deletePermanently(now)

        userRepository.clearActiveAccountByAccountId(
            accountId = accountId,
            now = now,
        )

        val deletedAccount = accountRepository.update(account)

        integrationEventPublisher.publish(
            AccountIntegrationEvents.deleted(
                account = deletedAccount,
                recipientUserIds = recipientUserIds,
                occurredAt = now,
            ),
        )
    }

    private fun createAdministrativeMember(
        account: Account,
        user: User,
        now: Instant,
    ): AccountMember {
        ensureAdministrativeMemberCapacity(account, now)

        val member =
            accountMemberRepository.create(
                AccountMember.create(
                    id = AccountMemberId.generate(),
                    accountId = account.id,
                    userId = user.id,
                ),
            )

        account.baseRoleId?.let { baseRoleId ->
            roleAssignmentRepository.create(
                RoleAssignment.createBaseRole(
                    accountId = account.id,
                    accountMemberId = member.id,
                    roleId = baseRoleId,
                    now = now,
                ),
            )
        }

        return member
    }

    private fun ensureAdministrativeMemberCapacity(
        account: Account,
        now: Instant,
    ) {
        val currentMemberCount = accountMemberRepository.countByAccountId(account.id)

        if (currentMemberCount < account.maxMembers) {
            return
        }

        account.updateMaxMembers(
            maxMembers = Math.addExact(currentMemberCount, 1),
            currentMemberCount = currentMemberCount,
            now = now,
        )
        accountRepository.update(account)
    }

    private fun publishMemberJoined(
        account: Account,
        member: AccountMember,
        now: Instant,
    ) {
        integrationEventPublisher.publish(
            AccountIntegrationEvents.memberJoined(
                account = account,
                member = member,
                recipientUserIds = accountMemberRepository.findUserIdsByAccountId(account.id),
                occurredAt = now,
            ),
        )
    }

    private fun requireAccountForUpdate(accountId: AccountId): Account =
        accountRepository.findByIdForUpdate(accountId)
            ?: throw AccountNotFoundException(accountId)

    private fun requireUser(userId: UserId): User =
        userRepository.findById(userId)
            ?: throw UserIdNotFoundException(userId)

    private fun requireMember(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): AccountMember =
        accountMemberRepository.findByIdAndAccountId(
            memberId = memberId,
            accountId = accountId,
        ) ?: throw AccountMemberNotFoundException(
            accountId = accountId,
            memberId = memberId,
        )

    private fun Account.toDetailsResponse(members: List<AccountMember>): AdminAccountDetailsResponse =
        AdminAccountDetailsResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid.id,
            cristalixNickname = cristalixNickname.value,
            status = status.name,
            ownerMemberId = ownerMemberId.id,
            maxMembers = maxMembers,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            deletedAt = deletedAt,
            members = members.map { member -> member.toResponse(this) },
        )

    private fun AccountMember.toResponse(account: Account): AdminAccountMemberResponse {
        val user = requireUser(userId)

        return AdminAccountMemberResponse(
            memberId = id.id,
            userId = userId.id,
            username = user.username.value,
            owner = account.isOwner(id),
            joinedAt = joinedAt,
        )
    }
}
