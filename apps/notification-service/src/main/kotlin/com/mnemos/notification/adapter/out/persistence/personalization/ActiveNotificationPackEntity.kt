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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value_mappings", columnDefinition = "jsonb")
    var valueMappings: Map<String, Any?>? = null,
    @Column(name = "last_revision", nullable = false)
    var lastRevision: Long,
    @Column(name = "last_event_id", nullable = false)
    var lastEventId: UUID,
    @Column(name = "last_event_at", nullable = false)
    var lastEventAt: Instant,
) {
    fun shouldApply(selectionRevision: Long): Boolean = selectionRevision > lastRevision

    fun select(
        personalizationId: UUID,
        mixedAggregateTitle: String,
        templates: Map<String, Any?>,
        valueMappings: Map<String, Any?> = emptyMap(),
        selectionRevision: Long,
        eventId: UUID,
        occurredAt: Instant,
    ) {
        this.personalizationId = personalizationId
        this.mixedAggregateTitle = mixedAggregateTitle
        this.templates = templates
        this.valueMappings = valueMappings
        lastRevision = selectionRevision
        lastEventId = eventId
        lastEventAt = occurredAt
    }

    fun clear(
        selectionRevision: Long,
        eventId: UUID,
        occurredAt: Instant,
    ) {
        personalizationId = null
        mixedAggregateTitle = null
        templates = null
        valueMappings = null
        lastRevision = selectionRevision
        lastEventId = eventId
        lastEventAt = occurredAt
    }
}
