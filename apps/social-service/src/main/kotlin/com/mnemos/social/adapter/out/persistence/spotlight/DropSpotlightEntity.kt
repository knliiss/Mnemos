package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "drop_spotlights")
class DropSpotlightEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "sequence", nullable = false, unique = true)
    var sequence: Long,
    @Column(name = "buyer_user_id", nullable = false)
    var buyerUserId: UUID,
    @Column(name = "source_account_id", nullable = false)
    var sourceAccountId: UUID,
    @Column(name = "item_drop_event_id", nullable = false)
    var itemDropEventId: UUID,
    @Column(name = "item_id", nullable = false)
    var itemId: UUID,
    @Column(name = "item_name", nullable = false, length = 128)
    var itemName: String,
    @Column(name = "item_rarity", nullable = false, length = 32)
    var itemRarity: String,
    @Column(name = "item_type", nullable = false, length = 32)
    var itemType: String,
    @Column(name = "dropped_for", nullable = false, length = 64)
    var droppedFor: String,
    @Column(name = "drop_occurred_at", nullable = false)
    var dropOccurredAt: Instant,
    @Column(name = "notification_pack_id")
    var notificationPackId: UUID?,
    @Column(name = "notification_pack_revision")
    var notificationPackRevision: Long?,
    @Column(name = "notification_pack_snapshot_json")
    var notificationPackSnapshotJson: String?,
    @Column(name = "price_prime", nullable = false)
    var pricePrime: Long,
    @Column(name = "wallet_request_id", nullable = false, unique = true)
    var walletRequestId: UUID,
    @Column(name = "wallet_transaction_id")
    var walletTransactionId: UUID?,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    var status: DropSpotlightStatus,
    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: Instant,
    @Column(name = "published_at")
    var publishedAt: Instant?,
    @Column(name = "expires_at")
    var expiresAt: Instant?,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
