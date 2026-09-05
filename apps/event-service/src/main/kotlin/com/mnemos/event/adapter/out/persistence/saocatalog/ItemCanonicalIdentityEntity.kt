package com.mnemos.event.adapter.out.persistence.saocatalog

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "item_canonical_identities")
class ItemCanonicalIdentityEntity(
    @Id
    @Column(name = "item_id", nullable = false)
    var itemId: UUID,
    @Column(name = "game_key", nullable = false, length = 160)
    var gameKey: String,
    @Column(name = "resolved_at", nullable = false)
    var resolvedAt: Instant,
)
