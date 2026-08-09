package com.mnemos.event.adapter.inbound.websocket.collector.dto

import java.util.UUID

data class CollectorEventReportAckMessage(
    val type: CollectorServerMessageType = CollectorServerMessageType.EVENT_REPORT_ACK,
    val messageId: UUID,
    val status: CollectorEventReportAckStatus,
    val confirmations: Int,
    val requiredConfirmations: Int,
)
