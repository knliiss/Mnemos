package com.mnemos.realtime.application.collector.security

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

data class CollectorAccessKey(
    val credentialId: CollectorCredentialId,
    val secret: String,
) {
    fun encode(): String = "${credentialId.id}.$secret"
}
