package com.mnemos.accountservice.application.service.member

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.application.policy.role.RoleAccessGuard
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class KickAccountMemberService(
    private val contextProvider: ActiveAccountContextProvider,
    private val roleAccessGuard: RoleAccessGuard,
    private val accountMemberRepository: AccountMemberRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun kickMember(
        accountId: AccountId,
        targetMemberId: AccountMemberId,
    ) {
        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = RolePermission.KICK_MEMBERS,
            )

        val targetMember =
            roleAccessGuard.requireCanKickMember(
                context = context,
                targetMemberId = targetMemberId,
            )

        val now = Instant.now()

        userRepository.clearActiveAccountIfMatches(
            userId = targetMember.userId,
            accountId = accountId,
            updatedAt = now,
        )

        accountMemberRepository.deleteById(targetMember.id)
    }
}
