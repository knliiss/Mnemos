package com.mnemos.bet.application.itemrace.model

import com.mnemos.bet.domain.itemrace.model.DomainItemType
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

data class ItemRaceView(
    val id: ItemRaceId,
    val itemName: String,
    val itemType: DomainItemType,
    val registrationClosesAt: Instant,
    val startsAt: Instant,
    val expiresAt: Instant,
    val entryAmount: Long,
    val feePercent: Int,
    val status: ItemRaceStatus,
    val winnerAccountId: AccountId?,
)
