package com.mnemos.realtime.application.collector.activation.usecase.redeem

data class RedeemCollectorActivationCommand(
    val token: String,
    val deviceName: String,
    val secretHash: String,
)
