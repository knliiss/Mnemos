package com.mnemos.realtime.adapter.inbound.http.collector

import com.mnemos.realtime.adapter.inbound.http.collector.CollectorActivationApiMapper.toResponse
import com.mnemos.realtime.application.collector.activation.usecase.create.CreateCollectorActivationUseCase
import com.mnemos.realtime.application.collector.activation.usecase.redeem.RedeemCollectorActivationCommand
import com.mnemos.realtime.application.collector.activation.usecase.redeem.RedeemCollectorActivationUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/realtime/collector-activations")
class CollectorActivationController(
    private val createActivationUseCase: CreateCollectorActivationUseCase,
    private val redeemActivationUseCase: RedeemCollectorActivationUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(): CollectorActivationResponse =
        createActivationUseCase
            .execute()
            .toResponse()

    @PostMapping("/redeem")
    fun redeem(
        @RequestBody
        @Valid
        request: RedeemCollectorActivationRequest,
    ): RedeemCollectorActivationResponse =
        redeemActivationUseCase
            .execute(
                RedeemCollectorActivationCommand(
                    token = request.token,
                    deviceName = request.deviceName,
                    secretHash = request.secretHash,
                ),
            ).toResponse()
}
