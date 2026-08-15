package com.mnemos.bet.adapter.inbound.http.common

import java.util.UUID

data class AcceptedBetOperationResponse(
    val resourceId: UUID,
    val operationId: UUID,
)
