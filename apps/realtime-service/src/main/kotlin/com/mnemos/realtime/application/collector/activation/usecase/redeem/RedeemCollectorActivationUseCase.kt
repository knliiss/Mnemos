package com.mnemos.realtime.application.collector.activation.usecase.redeem

import com.mnemos.realtime.application.collector.activation.model.RedeemCollectorActivationResult

fun interface RedeemCollectorActivationUseCase {
    fun execute(command: RedeemCollectorActivationCommand): RedeemCollectorActivationResult
}
