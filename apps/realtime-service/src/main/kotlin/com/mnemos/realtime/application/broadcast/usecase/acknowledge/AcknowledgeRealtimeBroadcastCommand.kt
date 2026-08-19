package com.mnemos.realtime.application.broadcast.usecase.acknowledge

import java.util.UUID

data class AcknowledgeRealtimeBroadcastCommand(
    val userId: UUID,
    val sequence: Long,
)
