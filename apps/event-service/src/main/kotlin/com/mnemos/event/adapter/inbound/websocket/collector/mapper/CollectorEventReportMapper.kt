package com.mnemos.event.adapter.inbound.websocket.collector.mapper

import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorEventReportAckMessage
import com.mnemos.event.adapter.inbound.websocket.collector.dto.CollectorEventReportAckStatus
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportResult
import com.mnemos.event.application.collector.usecase.submit.SubmitEventReportStatus
import java.util.UUID

object CollectorEventReportMapper {
    fun SubmitEventReportResult.toAck(messageId: UUID): CollectorEventReportAckMessage =
        CollectorEventReportAckMessage(
            messageId = messageId,
            status =
                when (status) {
                    SubmitEventReportStatus.ACCEPTED ->
                        CollectorEventReportAckStatus.ACCEPTED

                    SubmitEventReportStatus.DUPLICATE ->
                        CollectorEventReportAckStatus.DUPLICATE

                    SubmitEventReportStatus.RECORDED ->
                        CollectorEventReportAckStatus.RECORDED

                    SubmitEventReportStatus.ALREADY_RECORDED ->
                        CollectorEventReportAckStatus.ALREADY_RECORDED
                },
            confirmations = confirmations,
            requiredConfirmations = requiredConfirmations,
        )
}
