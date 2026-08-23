package com.mnemos.realtime.application.collector.activation.usecase.create

import com.mnemos.realtime.application.collector.activation.model.CollectorActivationView

fun interface CreateCollectorActivationUseCase {
    fun execute(): CollectorActivationView
}
