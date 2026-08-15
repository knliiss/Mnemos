package com.mnemos.social.adapter.out.persistence.spotlight

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notification_pack_selections")
class NotificationPackSelectionEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
    @Column(name = "personalization_id")
    var personalizationId: UUID?,
    @Column(name = "selection_revision", nullable = false)
    var selectionRevision: Long,
    @Column(name = "snapshot_json")
    var snapshotJson: String?,
    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,
)
