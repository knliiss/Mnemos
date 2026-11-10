package com.mnemos.telegram.bot.application.widget.management

import java.util.UUID

data class AccountManagementWidgetRole(
    val id: UUID,
    val name: String,
    val rank: Long,
    val permissionCount: Int,
)
