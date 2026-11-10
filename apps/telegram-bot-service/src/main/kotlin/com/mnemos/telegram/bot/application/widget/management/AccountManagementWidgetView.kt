package com.mnemos.telegram.bot.application.widget.management

import java.util.UUID

data class AccountManagementWidgetView(
    val accountId: UUID,
    val nickname: String,
    val page: Int,
    val pages: Int,
    val totalMembers: Int,
    val totalRoles: Int,
    val members: List<AccountManagementWidgetMember>,
    val roles: List<AccountManagementWidgetRole>,
    val canRemoveMembers: Boolean,
    val canAssignRoles: Boolean,
    val canManageRoles: Boolean = false,
    val feedback: String? = null,
    val pendingKickIndex: Int? = null,
)
