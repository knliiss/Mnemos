package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode

interface BetWidgetSnapshotQuery {
    fun load(
        telegramId: Long,
        mode: BetMenuMode,
    ): BetWidgetSnapshotResult
}
