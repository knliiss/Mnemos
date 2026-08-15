package com.mnemos.realtime.application.collector.credential.usecase.disable

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface DisableCollectorCredentialUseCase {
    fun execute(id: CollectorCredentialId): CollectorCredentialView
}
