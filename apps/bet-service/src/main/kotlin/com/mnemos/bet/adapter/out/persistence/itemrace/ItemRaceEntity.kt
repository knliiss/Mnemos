package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.domain.itemrace.model.DomainItemType
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "item_races")
class ItemRaceEntity(
    @Id
    var id: UUID,
    @Column(name = "item_name", nullable = false)
    var itemName: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    var itemType: DomainItemType,
    @Column(name = "registration_closes_at", nullable = false)
    var registrationClosesAt: Instant,
    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    @Column(name = "entry_amount", nullable = false)
    var entryAmount: Long,
    @Column(name = "fee_percent", nullable = false)
    var feePercent: Int,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ItemRaceStatus,
    @Column(name = "winner_account_id")
    var winnerAccountId: UUID?,
)
