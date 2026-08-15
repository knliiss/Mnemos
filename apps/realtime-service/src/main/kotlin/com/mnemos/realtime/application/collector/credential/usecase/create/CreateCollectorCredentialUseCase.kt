package com.mnemos.realtime.application.collector.credential.usecase.create

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView

interface CreateCollectorCredentialUseCase {
    fun execute(command: CreateCollectorCredentialCommand): CollectorCredentialView
}
