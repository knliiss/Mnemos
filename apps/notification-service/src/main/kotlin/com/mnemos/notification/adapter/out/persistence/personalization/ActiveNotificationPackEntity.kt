package com.mnemos.notification.adapter.out.persistence.personalization

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "active_notification_packs")
class ActiveNotificationPackEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "personalization_id", nullable = false)
    var personalizationId: UUID,
    @Column(name = "mixed_aggregate_title", nullable = false, length = 512)
    var mixedAggregateTitle: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "templates", nullable = false, columnDefinition = "jsonb")
    var templates: Map<String, Any?>,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
