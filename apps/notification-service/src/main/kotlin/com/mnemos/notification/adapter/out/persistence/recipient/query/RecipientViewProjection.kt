package com.mnemos.notification.adapter.out.persistence.recipient.query

import com.mnemos.notification.domain.recipient.model.RecipientPriority
import java.util.UUID

data class RecipientViewProjection(
    val userId: UUID,
    val telegramId: Long,
    val priority: RecipientPriority,
)
