package com.mnemos.telegram.bot.application.widget.management

interface ChangeAccountManagementWidgetUseCase {
    fun assignRole(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        memberIndex: Int,
        roleIndex: Int,
        assigned: Boolean,
    ): AccountManagementWidgetMutationResult

    fun deleteRole(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        roleIndex: Int,
    ): AccountManagementWidgetMutationResult

    fun kickMember(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        memberIndex: Int,
    ): AccountManagementWidgetMutationResult
}
