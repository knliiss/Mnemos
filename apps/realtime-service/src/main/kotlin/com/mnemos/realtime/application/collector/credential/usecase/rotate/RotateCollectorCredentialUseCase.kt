package com.mnemos.realtime.application.collector.credential.usecase.rotate

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface RotateCollectorCredentialUseCase {
    fun execute(id: CollectorCredentialId): CollectorCredentialView
}
