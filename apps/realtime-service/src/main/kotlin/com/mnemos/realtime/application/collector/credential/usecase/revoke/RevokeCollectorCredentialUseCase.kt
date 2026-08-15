package com.mnemos.realtime.application.collector.credential.usecase.revoke

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface RevokeCollectorCredentialUseCase {
    fun execute(id: CollectorCredentialId): CollectorCredentialView
}
