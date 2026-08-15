package com.mnemos.realtime.application.collector.credential.usecase.enable

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface EnableCollectorCredentialUseCase {
    fun execute(id: CollectorCredentialId): CollectorCredentialView
}
