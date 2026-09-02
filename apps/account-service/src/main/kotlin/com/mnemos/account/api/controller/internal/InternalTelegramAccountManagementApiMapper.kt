package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.service.account.InternalTelegramAccountManagement
import com.mnemos.account.application.service.account.InternalTelegramAccountMember
import com.mnemos.account.application.service.account.InternalTelegramAccountRole
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountManagementResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountMemberResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountPermission
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountRoleResponse

object InternalTelegramAccountManagementApiMapper {
    fun InternalTelegramAccountManagement.toResponse(): InternalTelegramAccountManagementResponse =
        InternalTelegramAccountManagementResponse(
            accountId = accountId.id,
            cristalixNickname = cristalixNickname,
            actorMemberId = actorMemberId.id,
            actorPermissions = actorPermissions.mapTo(linkedSetOf()) { permission -> permission.toContract() },
            members = members.map { member -> member.toResponse() },
            roles = roles.map { role -> role.toResponse() },
        )

    fun InternalTelegramAccountPermission.toDomain(): RolePermission = RolePermission.valueOf(name)

    private fun InternalTelegramAccountMember.toResponse(): InternalTelegramAccountMemberResponse =
        InternalTelegramAccountMemberResponse(
            id = id.id,
            name = name,
            owner = owner,
            highestRoleName = highestRoleName,
            highestRoleColor = highestRoleColor,
            roleIds = roleIds.mapTo(linkedSetOf()) { roleId -> roleId.id },
            joinedAt = joinedAt,
        )

    private fun InternalTelegramAccountRole.toResponse(): InternalTelegramAccountRoleResponse =
        InternalTelegramAccountRoleResponse(
            id = id.id,
            name = name,
            color = color,
            hierarchyRank = hierarchyRank,
            permissions = permissions.mapTo(linkedSetOf()) { permission -> permission.toContract() },
        )

    private fun RolePermission.toContract(): InternalTelegramAccountPermission = InternalTelegramAccountPermission.valueOf(name)
}
