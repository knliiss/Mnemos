package com.mnemos.contracts.account.internal.v1

import java.time.Instant
import java.util.UUID

data class InternalTelegramAccountManagementResponse(
    val accountId: UUID,
    val cristalixNickname: String,
    val actorMemberId: UUID,
    val actorPermissions: Set<InternalTelegramAccountPermission>,
    val members: List<InternalTelegramAccountMemberResponse>,
    val roles: List<InternalTelegramAccountRoleResponse>,
)

data class InternalTelegramAccountMemberResponse(
    val id: UUID,
    val name: String,
    val owner: Boolean,
    val highestRoleName: String?,
    val highestRoleColor: String?,
    val roleIds: Set<UUID>,
    val joinedAt: Instant,
)

data class InternalTelegramAccountRoleResponse(
    val id: UUID,
    val name: String,
    val color: String,
    val hierarchyRank: Long,
    val permissions: Set<InternalTelegramAccountPermission>,
)

data class CreateInternalTelegramAccountRoleRequest(
    val name: String,
    val color: String,
)

data class UpdateInternalTelegramAccountRoleDetailsRequest(
    val name: String? = null,
    val color: String? = null,
)

data class SetInternalTelegramAccountRolePermissionRequest(
    val permission: InternalTelegramAccountPermission,
    val enabled: Boolean,
)

data class MoveInternalTelegramAccountRoleRequest(
    val targetIndex: Int,
)

enum class InternalTelegramAccountPermission {
    MANAGE_MEMBERS,
    ASSIGN_MEMBER_ROLES,
    MANAGE_ROLES,
    MANAGE_ROLE_HIERARCHY,
    MANAGE_BASE_ROLE,
    VIEW_TRANSACTION_HISTORY,
    PLACE_BETS,
    BUY_ACCOUNT_PERSONALIZATIONS,
    APPLY_ACCOUNT_PERSONALIZATIONS,
    MODERATE_COMMENTS,
}
