package com.mnemos.realtime.application.collector.credential

import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Component

@Component
class RequireCollectorCredential(
    private val repository: CollectorCredentialRepository,
) {
    fun find(id: CollectorCredentialId): CollectorCredential =
        repository.findById(id)
            ?: throw CollectorCredentialNotFoundException(id)
}
