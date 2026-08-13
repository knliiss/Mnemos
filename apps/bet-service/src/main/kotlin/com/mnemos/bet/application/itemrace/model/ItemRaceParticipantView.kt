package com.mnemos.bet.application.itemrace.model

import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
import com.mnemos.common.core.model.id.AccountId

data class ItemRaceParticipantView(
    val id: ItemRaceParticipantId,
    val raceId: ItemRaceId,
    val accountId: AccountId,
    val accountNickname: String,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: ItemRaceParticipantStatus,
)
