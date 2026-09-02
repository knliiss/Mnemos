package com.mnemos.telegram.bot.application.account.management.port.out

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import java.util.UUID

interface AccountManagementGateway {
    fun getManagement(telegramId: Long): TelegramAccountManagementResult

    fun kickMember(
        telegramId: Long,
        memberId: UUID,
    ): TelegramAccountManagementMutationResult

    fun setMemberRole(
        telegramId: Long,
        memberId: UUID,
        roleId: UUID,
        assigned: Boolean,
    ): TelegramAccountManagementMutationResult

    fun createRole(
        telegramId: Long,
        name: String,
        color: String,
    ): TelegramAccountManagementMutationResult

    fun updateRoleDetails(
        telegramId: Long,
        roleId: UUID,
        name: String,
        color: String,
    ): TelegramAccountManagementMutationResult

    fun setRolePermission(
        telegramId: Long,
        roleId: UUID,
        permission: TelegramAccountManagementPermission,
        enabled: Boolean,
    ): TelegramAccountManagementMutationResult

    fun moveRole(
        telegramId: Long,
        roleId: UUID,
        targetIndex: Int,
    ): TelegramAccountManagementMutationResult

    fun deleteRole(
        telegramId: Long,
        roleId: UUID,
    ): TelegramAccountManagementMutationResult
}
