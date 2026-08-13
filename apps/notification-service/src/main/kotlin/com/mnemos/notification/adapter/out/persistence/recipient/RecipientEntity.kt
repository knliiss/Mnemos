package com.mnemos.notification.adapter.out.persistence.recipient

import com.mnemos.notification.domain.recipient.model.RecipientDeliveryStatus
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "recipients")
class RecipientEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "telegram_id", nullable = false, unique = true)
    var telegramId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    var priority: RecipientPriority,
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    var deliveryStatus: RecipientDeliveryStatus,
)
