package com.mnemos.realtime.adapter.inbound.http.collector

import com.mnemos.realtime.application.collector.activation.model.CollectorActivationView
import com.mnemos.realtime.application.collector.activation.model.RedeemCollectorActivationResult

object CollectorActivationApiMapper {
    fun CollectorActivationView.toResponse(): CollectorActivationResponse =
        CollectorActivationResponse(
            token = token,
            expiresAt = expiresAt,
        )

    fun RedeemCollectorActivationResult.toResponse(): RedeemCollectorActivationResponse =
        RedeemCollectorActivationResponse(
            credentialId = credentialId.id,
        )
}
