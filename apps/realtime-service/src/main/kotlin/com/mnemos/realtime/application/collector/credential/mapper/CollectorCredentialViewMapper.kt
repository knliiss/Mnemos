package com.mnemos.realtime.application.collector.credential.mapper

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.application.collector.security.CollectorAccessKey
import com.mnemos.realtime.domain.collector.model.CollectorCredential

object CollectorCredentialViewMapper {
    fun CollectorCredential.toView(secret: String? = null): CollectorCredentialView =
        CollectorCredentialView(
            id = id,
            name = name,
            enabled = enabled,
            createdAt = createdAt,
            updatedAt = updatedAt,
            revokedAt = revokedAt,
            accessKey =
                secret?.let {
                    CollectorAccessKey(
                        credentialId = id,
                        secret = it,
                    ).encode()
                },
        )
}
