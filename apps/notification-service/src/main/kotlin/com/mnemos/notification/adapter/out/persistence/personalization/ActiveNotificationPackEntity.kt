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

    @Column(name = "personalization_id")
    var personalizationId: UUID?,

    @Column(name = "mixed_aggregate_title", length = 512)
    var mixedAggregateTitle: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "templates", columnDefinition = "jsonb")
    var templates: Map<String, Any?>?,

    @Column(name = "last_event_id", nullable = false)
    var lastEventId: UUID,

    @Column(name = "last_event_at", nullable = false)
    var lastEventAt: Instant,
) {
    fun shouldApply(
        eventId: UUID,
        occurredAt: Instant,
    ): Boolean {
        if (occurredAt != lastEventAt) {
            return occurredAt.isAfter(lastEventAt)
        }

        return eventId.toString() > lastEventId.toString()
    }

    fun select(
        personalizationId: UUID,
        mixedAggregateTitle: String,
        templates: Map<String, Any?>,
        eventId: UUID,
        occurredAt: Instant,
    ) {
        this.personalizationId = personalizationId
        this.mixedAggregateTitle = mixedAggregateTitle
        this.templates = templates
        lastEventId = eventId
        lastEventAt = occurredAt
    }

    fun clear(
        eventId: UUID,
        occurredAt: Instant,
    ) {
        personalizationId = null
        mixedAggregateTitle = null
        templates = null
        lastEventId = eventId
        lastEventAt = occurredAt
    }
}
