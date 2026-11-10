package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import java.util.UUID

data class AccountManagementWidgetRole(
    val id: UUID,
    val name: String,
    val rank: Long,
    val permissionCount: Int,
    val color: String = "#FFFFFF",
    val permissions: Set<TelegramAccountManagementPermission> = emptySet(),
    val canEditPermissions: Boolean = true,
)
