package com.mnemos.realtime.application.collector.credential.usecase.list

import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView

fun interface ListCollectorCredentialsUseCase {
    fun execute(): List<CollectorCredentialView>
}
