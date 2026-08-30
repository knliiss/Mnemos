package com.mnemos.telegram.bot.application.menu.model

import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult

sealed interface TelegramMenuScreenData {
    data object Empty : TelegramMenuScreenData

    data class SaoOnline(
        val result: SaoOnlineSnapshotResult,
    ) : TelegramMenuScreenData
}
