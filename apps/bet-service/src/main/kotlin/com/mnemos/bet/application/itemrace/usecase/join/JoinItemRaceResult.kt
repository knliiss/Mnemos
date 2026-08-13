package com.mnemos.bet.application.itemrace.usecase.join

import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId

data class JoinItemRaceResult(
    val participantId: ItemRaceParticipantId,
)
