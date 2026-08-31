package com.mnemos.telegram.bot.contracts.internal.bet

import java.util.UUID

data class TelegramItemRaceParticipantResponse(
    val id: UUID,
    val raceId: UUID,
    val accountNickname: String,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: String,
)
