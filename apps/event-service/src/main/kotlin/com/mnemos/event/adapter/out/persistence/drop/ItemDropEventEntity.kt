package com.mnemos.event.adapter.out.persistence.drop

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "item_drop_events")
class ItemDropEventEntity(
    @Id
    var id: UUID,
    @Column(name = "item_id", nullable = false)
    var itemId: UUID,
    @Column(name = "player_id")
    var playerId: UUID?,
    @Column(name = "dropped_for", nullable = false)
    var droppedFor: String,
    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,
    @Column(name = "identity_resolution_pending", nullable = false)
    var identityResolutionPending: Boolean,
    @Column(name = "identity_resolution_requested_at")
    var identityResolutionRequestedAt: Instant?,
)
