package com.mnemos.realtime.application.delivery.usecase.replay

import java.util.UUID

data class GetRealtimeReplayQuery(
    val userId: UUID,
    val lastEventId: UUID?,
)
