package com.mnemos.telegram.bot.application.online.port.out

import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult

interface SaoOnlineSnapshotGateway {
    fun getSnapshot(): SaoOnlineSnapshotResult
}
