package com.mnemos.account.application.service.member

import com.mnemos.account.application.context.account.ActiveAccountContext
import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.user.UserRepository
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
                permission = RolePermission.MANAGE_MEMBERS,
            )

        kickMember(
            context = context,
            targetMemberId = targetMemberId,
        )
    }

    @Transactional
    fun kickMember(
        context: ActiveAccountContext,
        targetMemberId: AccountMemberId,
    ) {
        val targetMember =
            roleAccessGuard.requireCanKickMember(
                context = context,
                targetMemberId = targetMemberId,
            )

        val now = Instant.now()

        userRepository.clearActiveAccountIfMatches(
            userId = targetMember.userId,
            accountId = context.account.id,
            updatedAt = now,
        )

        accountMemberRepository.deleteById(targetMember.id)
    }
}
