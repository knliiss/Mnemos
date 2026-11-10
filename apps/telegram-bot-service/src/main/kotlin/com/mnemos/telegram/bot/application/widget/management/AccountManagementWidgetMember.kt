package com.mnemos.telegram.bot.application.widget.management

import java.util.UUID

data class AccountManagementWidgetMember(
    val id: UUID,
    val name: String,
    val role: String?,
    val owner: Boolean,
    val self: Boolean,
    val roleIds: Set<UUID>,
)
