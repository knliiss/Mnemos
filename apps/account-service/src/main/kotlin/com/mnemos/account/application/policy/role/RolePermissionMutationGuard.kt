package com.mnemos.account.application.policy.role

import com.mnemos.account.application.context.account.ActiveAccountContext
import com.mnemos.account.domain.exception.role.RoleAccessDeniedException
import com.mnemos.account.domain.exception.role.RoleAccessDeniedReason
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.repository.role.RoleRepository
import org.springframework.stereotype.Component

@Component
class RolePermissionMutationGuard(
    private val roleRepository: RoleRepository,
) {
    fun requireRoleNotAssignedToActor(
        context: ActiveAccountContext,
        role: Role,
    ) {
        if (context.account.isOwner(context.member.id)) {
            return
        }

        val actorRoleIds =
            roleRepository
                .findAllByMemberId(
                    accountId = context.account.id,
                    memberId = context.member.id,
                ).mapTo(mutableSetOf(), Role::id)

        if (role.id in actorRoleIds) {
            throw RoleAccessDeniedException(
                accountId = context.account.id,
                reason = RoleAccessDeniedReason.SELF_ROLE_MANAGEMENT,
            )
        }
    }
}
