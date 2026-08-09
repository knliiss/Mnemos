package com.mnemos.account.domain.model.account.role.assignment

import com.mnemos.common.core.model.id.UserId

sealed class RoleAssignmentActor {
    data class User(
        val userId: UserId,
    ) : RoleAssignmentActor()

    data object System : RoleAssignmentActor()
}
