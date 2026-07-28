package com.mnemos.accountservice.application.service.member

import com.mnemos.accountservice.application.context.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.account.member.AccountMemberAlreadyExistsException
import com.mnemos.accountservice.domain.exception.invite.AccountInviteUnavailableException
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.AccountStatus
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.invite.AccountInviteRepository
import com.mnemos.accountservice.domain.repository.role.RoleAssignmentRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class JoinAccountService(
    private val currentUserProvider: CurrentUserProvider,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val inviteRepository: AccountInviteRepository,
    private val userRepository: UserRepository,
    private val roleAssignmentRepository: RoleAssignmentRepository,
) {
    @Transactional
    fun joinAccount(rawInviteCode: String): JoinAccountResult {
        val user = currentUserProvider.get()
        val inviteCode = parseInviteCode(rawInviteCode)

        val invitePreview =
            inviteRepository.findByCode(inviteCode)
                ?: throw AccountInviteUnavailableException()

        val account =
            accountRepository.findByIdForUpdate(
                invitePreview.accountId,
            ) ?: throw AccountInviteUnavailableException()

        if (account.status != AccountStatus.ACTIVE) {
            throw AccountInviteUnavailableException()
        }

        val invite =
            inviteRepository.findByIdAndAccountIdForUpdate(
                inviteId = invitePreview.id,
                accountId = account.id,
            ) ?: throw AccountInviteUnavailableException()

        if (
            accountMemberRepository.existsByAccountIdAndUserId(
                accountId = account.id,
                userId = user.id,
            )
        ) {
            throw AccountMemberAlreadyExistsException(
                accountId = account.id,
                userId = user.id,
            )
        }

        val currentMemberCount =
            accountMemberRepository.countByAccountId(
                account.id,
            )

        account.requireAvailableMemberSlot(currentMemberCount)

        val now = Instant.now()

        invite.consume(now)

        val memberId = AccountMemberId.generate()
        val member =
            AccountMember.create(
                id = memberId,
                accountId = account.id,
                userId = user.id,
            )

        val createdMember = accountMemberRepository.create(member)

        account.baseRoleId?.let { baseRoleId ->
            roleAssignmentRepository.create(
                RoleAssignment.createBaseRole(
                    accountId = account.id,
                    accountMemberId = createdMember.id,
                    roleId = baseRoleId,
                    now = now,
                ),
            )
        }

        user.changeActiveAccount(account.id)

        inviteRepository.update(invite)
        userRepository.update(user)

        return JoinAccountResult(
            accountId = account.id,
            memberId = createdMember.id,
        )
    }

    private fun parseInviteCode(rawInviteCode: String): InviteCode =
        try {
            InviteCode.of(rawInviteCode)
        } catch (_: IllegalArgumentException) {
            throw AccountInviteUnavailableException()
        }
}

data class JoinAccountResult(
    val accountId: AccountId,
    val memberId: AccountMemberId,
)
