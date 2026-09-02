package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountManagementResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountMemberResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountPermission
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountRoleResponse
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMember
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementRole
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementSnapshot

object AccountManagementApiMapper {
    fun InternalTelegramAccountManagementResponse.toModel(): TelegramAccountManagementSnapshot =
        TelegramAccountManagementSnapshot(
            accountId = accountId,
            cristalixNickname = cristalixNickname,
            actorMemberId = actorMemberId,
            actorPermissions = actorPermissions.mapTo(linkedSetOf()) { permission -> permission.toModel() },
            members = members.map { member -> member.toModel() },
            roles = roles.map { role -> role.toModel() },
        )

    fun TelegramAccountManagementPermission.toContract(): InternalTelegramAccountPermission =
        InternalTelegramAccountPermission.valueOf(name)

    private fun InternalTelegramAccountMemberResponse.toModel(): TelegramAccountManagementMember =
        TelegramAccountManagementMember(
            id = id,
            name = name,
            owner = owner,
            highestRoleName = highestRoleName,
            highestRoleColor = highestRoleColor,
            roleIds = roleIds,
            joinedAt = joinedAt,
        )

    private fun InternalTelegramAccountRoleResponse.toModel(): TelegramAccountManagementRole =
        TelegramAccountManagementRole(
            id = id,
            name = name,
            color = color,
            hierarchyRank = hierarchyRank,
            permissions = permissions.mapTo(linkedSetOf()) { permission -> permission.toModel() },
        )

    private fun InternalTelegramAccountPermission.toModel(): TelegramAccountManagementPermission =
        TelegramAccountManagementPermission.valueOf(name)
}
