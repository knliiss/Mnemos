package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
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
        if (member.owner || member.self) return AccountManagementWidgetMutationResult.FORBIDDEN
        if ((role.id in member.roleIds) == assigned) return AccountManagementWidgetMutationResult.APPLIED
        if (!guard.claimQuery(telegramId, queryId)) return AccountManagementWidgetMutationResult.ALREADY
        return gateway.setMemberRole(telegramId, member.id, role.id, assigned).toWidgetResult()
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
