package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import java.util.UUID

data class BetWidgetView(
    val accountId: UUID?,
    val mode: BetMenuMode,
    val totalItems: Int,
    val entries: List<BetWidgetEntry>,
)
