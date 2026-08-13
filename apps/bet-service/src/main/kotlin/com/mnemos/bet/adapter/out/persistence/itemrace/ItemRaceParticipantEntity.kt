package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "item_race_participants")
class ItemRaceParticipantEntity(
    @Id
    var id: UUID,
    @Column(name = "race_id", nullable = false)
    var raceId: UUID,
    @Column(name = "account_id", nullable = false)
    var accountId: UUID,
    @Column(name = "account_nickname", nullable = false)
    var accountNickname: String,
    @Column(name = "confirmed_amount", nullable = false)
    var confirmedAmount: Long,
    @Column(name = "pending_amount", nullable = false)
    var pendingAmount: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ItemRaceParticipantStatus,
)
