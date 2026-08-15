package com.mnemos.realtime.application.collector.security

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service

@Service
class AuthenticateCollectorService(
    private val repository: CollectorCredentialRepository,
    private val secretHasher: CollectorSecretHasher,
) {
    fun authenticate(accessKey: CollectorAccessKey): CollectorCredentialId? {
        val credential = repository.findById(accessKey.credentialId) ?: return null

        if (!credential.enabled || credential.revokedAt != null) {
            return null
        }

        return credential.id.takeIf {
            secretHasher.matches(
                secret = accessKey.secret,
                expectedHash = credential.secretHash,
            )
        }
    }

    fun isActive(collectorId: CollectorCredentialId): Boolean {
        val credential = repository.findById(collectorId) ?: return false

        return credential.enabled && credential.revokedAt == null
    }
}
