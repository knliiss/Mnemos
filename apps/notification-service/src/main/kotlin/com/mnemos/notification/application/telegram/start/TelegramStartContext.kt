package com.mnemos.notification.application.telegram.start

import com.mnemos.common.core.model.id.UserId

data class TelegramStartContext(
    val telegramId: Long,
    val firstName: String? = null,
    val username: String? = null,
    val parameter: String? = null,
    val userId: UserId? = null,
)
