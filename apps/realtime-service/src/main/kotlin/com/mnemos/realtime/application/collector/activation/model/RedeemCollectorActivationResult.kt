package com.mnemos.realtime.application.collector.activation.model

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

data class RedeemCollectorActivationResult(
    val credentialId: CollectorCredentialId,
)
