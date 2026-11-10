package com.mnemos.telegram.bot.application.widget.management

import java.util.UUID

object AccountManagementWidgetIdentity {
    fun tag(id: UUID): String = id.toString().replace("-", "").take(12)
}
