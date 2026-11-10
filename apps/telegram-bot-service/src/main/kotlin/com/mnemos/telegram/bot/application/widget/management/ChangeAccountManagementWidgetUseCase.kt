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

    fun setRolePermission(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        roleIndex: Int,
        permissionIndex: Int,
        enabled: Boolean,
    ): AccountManagementWidgetMutationResult

    fun moveRole(
        telegramId: Long,
        queryId: String,
        view: AccountManagementWidgetView,
        roleIndex: Int,
        offset: Int,
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
