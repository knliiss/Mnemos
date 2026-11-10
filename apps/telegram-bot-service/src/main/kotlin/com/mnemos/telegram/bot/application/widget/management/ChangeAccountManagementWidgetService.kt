package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetMutationGuard
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class ChangeAccountManagementWidgetService(
    private val gateway: AccountManagementGateway,
    private val guard: AccountManagementWidgetMutationGuard,
) : ChangeAccountManagementWidgetUseCase {
    override fun assignRole(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        memberIndex: Int,
        roleIndex: Int,
        assigned: Boolean,
    ): AccountManagementWidgetMutationResult {
        if (!view.canAssignRoles) return AccountManagementWidgetMutationResult.FORBIDDEN
        val member = view.members.getOrNull(memberIndex) ?: return AccountManagementWidgetMutationResult.CONFLICT
        val role = view.roles.getOrNull(roleIndex) ?: return AccountManagementWidgetMutationResult.CONFLICT
        if ((member.owner || member.self) && !view.actorIsOwner) {
            return AccountManagementWidgetMutationResult.FORBIDDEN
        }
        if ((role.id in member.roleIds) == assigned) return AccountManagementWidgetMutationResult.APPLIED
        if (!guard.claimQuery(telegramId, queryId)) return AccountManagementWidgetMutationResult.ALREADY
        return gateway.setMemberRole(telegramId, member.id, role.id, assigned).toWidgetResult()
    }

    override fun setRolePermission(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        roleIndex: Int,
        permissionIndex: Int,
        enabled: Boolean,
    ): AccountManagementWidgetMutationResult {
        if (!view.canManageRoles) return AccountManagementWidgetMutationResult.FORBIDDEN
        val role = view.roles.getOrNull(roleIndex) ?: return AccountManagementWidgetMutationResult.CONFLICT
        val permission = TelegramAccountManagementPermission.entries.getOrNull(permissionIndex)
            ?: return AccountManagementWidgetMutationResult.INVALID
        if (!role.canEditPermissions) return AccountManagementWidgetMutationResult.FORBIDDEN
        if ((permission in role.permissions) == enabled) return AccountManagementWidgetMutationResult.APPLIED
        if (!guard.claimQuery(telegramId, queryId)) return AccountManagementWidgetMutationResult.ALREADY
        return gateway.setRolePermission(telegramId, role.id, permission, enabled).toWidgetResult()
    }

    override fun moveRole(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        roleIndex: Int,
        offset: Int,
    ): AccountManagementWidgetMutationResult {
        if (!view.canManageHierarchy) return AccountManagementWidgetMutationResult.FORBIDDEN
        if (offset !in listOf(-1, 1)) return AccountManagementWidgetMutationResult.INVALID
        val role = view.roles.getOrNull(roleIndex) ?: return AccountManagementWidgetMutationResult.CONFLICT
        val snapshot = (gateway.getManagement(telegramId) as? TelegramAccountManagementResult.Available)?.snapshot
            ?: return AccountManagementWidgetMutationResult.UNAVAILABLE
        if (snapshot.accountId != view.accountId) return AccountManagementWidgetMutationResult.CONFLICT
        if (TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY !in snapshot.actorPermissions) {
            return AccountManagementWidgetMutationResult.FORBIDDEN
        }
        val ordered = snapshot.roles.sortedBy { it.hierarchyRank }
        val index = ordered.indexOfFirst { it.id == role.id && it.hierarchyRank == role.rank }
        if (index < 0) return AccountManagementWidgetMutationResult.CONFLICT
        val targetIndex = index + offset
        if (targetIndex !in ordered.indices) return AccountManagementWidgetMutationResult.INVALID
        if (!guard.claimQuery(telegramId, queryId)) return AccountManagementWidgetMutationResult.ALREADY
        return gateway.moveRole(telegramId, role.id, targetIndex).toWidgetResult()
    }

    override fun deleteRole(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        roleIndex: Int,
    ): AccountManagementWidgetMutationResult {
        if (!view.canManageRoles) return AccountManagementWidgetMutationResult.FORBIDDEN
        val role = view.roles.getOrNull(roleIndex) ?: return AccountManagementWidgetMutationResult.CONFLICT
        if (!guard.claimQuery(telegramId, queryId)) return AccountManagementWidgetMutationResult.ALREADY
        return gateway.deleteRole(telegramId, role.id).toWidgetResult()
    }

    override fun kickMember(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        memberIndex: Int,
    ): AccountManagementWidgetMutationResult {
        if (!view.canRemoveMembers) return AccountManagementWidgetMutationResult.FORBIDDEN
        val member = view.members.getOrNull(memberIndex) ?: return AccountManagementWidgetMutationResult.CONFLICT
        if (member.owner || member.self) return AccountManagementWidgetMutationResult.FORBIDDEN
        if (!guard.claimQuery(telegramId, queryId)) return AccountManagementWidgetMutationResult.ALREADY
        return gateway.kickMember(telegramId, member.id).toWidgetResult()
    }

    private fun TelegramAccountManagementMutationResult.toWidgetResult(): AccountManagementWidgetMutationResult =
        when (this) {
            TelegramAccountManagementMutationResult.UPDATED -> AccountManagementWidgetMutationResult.APPLIED
            TelegramAccountManagementMutationResult.INVALID -> AccountManagementWidgetMutationResult.INVALID
            TelegramAccountManagementMutationResult.ACTIVE_ACCOUNT_REQUIRED -> AccountManagementWidgetMutationResult.ACCOUNT_REQUIRED
            TelegramAccountManagementMutationResult.FORBIDDEN -> AccountManagementWidgetMutationResult.FORBIDDEN
            TelegramAccountManagementMutationResult.REFRESH -> AccountManagementWidgetMutationResult.CONFLICT
            TelegramAccountManagementMutationResult.UNAVAILABLE -> AccountManagementWidgetMutationResult.UNAVAILABLE
        }
}
