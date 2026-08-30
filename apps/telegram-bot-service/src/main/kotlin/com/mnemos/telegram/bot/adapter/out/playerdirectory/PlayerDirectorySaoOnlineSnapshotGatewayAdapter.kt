package com.mnemos.telegram.bot.adapter.out.playerdirectory

import com.mnemos.telegram.bot.adapter.out.playerdirectory.PlayerDirectorySaoOnlineMapper.toModel
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PlayerDirectorySaoOnlineSnapshotGatewayAdapter(
    private val client: PlayerDirectoryServiceClient,
) : SaoOnlineSnapshotGateway {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getSnapshot(): SaoOnlineSnapshotResult {
        val result =
            try {
                SaoOnlineSnapshotResult.Available(client.getSaoSnapshot().toModel())
            } catch (exception: RuntimeException) {
                logger.warn("Could not load SAO realm snapshot from player-directory-service", exception)
                SaoOnlineSnapshotResult.Unavailable
            }

        return result
    }
}
