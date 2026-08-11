package com.mnemos.notification.application.notification.delivery.model

import com.mnemos.notification.application.notification.policy.EventPriority
import com.mnemos.notification.domain.recipient.model.RecipientPriority

data class DeliveryPriority(
    val event: EventPriority,
    val recipient: RecipientPriority,
)
