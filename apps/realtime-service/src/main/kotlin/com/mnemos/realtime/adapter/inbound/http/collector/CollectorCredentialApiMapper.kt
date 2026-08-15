package com.mnemos.realtime.adapter.inbound.http.collector

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView

object CollectorCredentialApiMapper {
    fun CollectorCredentialView.toResponse(): CollectorCredentialResponse =
        CollectorCredentialResponse(
            id = id.id,
            name = name,
            enabled = enabled,
            createdAt = createdAt,
            updatedAt = updatedAt,
            revokedAt = revokedAt,
            accessKey = accessKey,
        )
}
