package com.mnemos.account.application.policy.role

import com.mnemos.account.domain.exception.role.RoleAccessDeniedException
import com.mnemos.account.domain.exception.role.RoleAccessDeniedReason
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Component

@Component
class RolePermissionGrantPolicy {
    fun requireAllowed(
        accountId: AccountId,
        isOwner: Boolean,
        actorPermissions: Set<RolePermission>,
        requestedPermissions: Collection<RolePermission>,
    ) {
        if (isOwner) {
            return
        }

        if (!actorPermissions.containsAll(requestedPermissions)) {
            throw RoleAccessDeniedException(
                accountId = accountId,
                reason = RoleAccessDeniedReason.PERMISSION_ESCALATION,
            )
        }
    }
}
