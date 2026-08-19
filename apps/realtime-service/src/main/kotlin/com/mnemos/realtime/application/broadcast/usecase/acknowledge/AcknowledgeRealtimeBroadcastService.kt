package com.mnemos.realtime.application.broadcast.usecase.acknowledge

import com.mnemos.realtime.application.broadcast.exception.RealtimeBroadcastNotFoundException
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import org.springframework.stereotype.Service

@Service
class AcknowledgeRealtimeBroadcastService(
    private val replayStore: RealtimeBroadcastReplayStore,
) {
    fun execute(command: AcknowledgeRealtimeBroadcastCommand) {
        val acknowledged =
            replayStore.acknowledge(
                userId = command.userId,
                sequence = command.sequence,
            )

        if (!acknowledged) {
            throw RealtimeBroadcastNotFoundException(command.sequence)
        }
    }
}
