package com.mnemos.accountservice.domain.model.account.role.assignment

import com.mnemos.common.core.model.user.UserId

sealed class RoleAssignmentActor {
    data class User(
        val userId: UserId,
    ) : RoleAssignmentActor()

    data object System : RoleAssignmentActor()
}
