package com.mnemos.notification.adapter.inbound.http.raid

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.raid.create.CreateRaidSubscriptionApiMapper.toCommand
import com.mnemos.notification.adapter.inbound.http.raid.create.CreateRaidSubscriptionApiMapper.toResponse
import com.mnemos.notification.adapter.inbound.http.raid.create.CreateRaidSubscriptionRequest
import com.mnemos.notification.adapter.inbound.http.raid.create.CreateRaidSubscriptionResponse
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionUseCase
import com.mnemos.notification.application.raid.usecase.update.UpdateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.update.UpdateRaidSubscriptionUseCase
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions/raids")
class RaidSubscriptionController(
    private val createRaidSubscriptionUseCase: CreateRaidSubscriptionUseCase,
    private val updateRaidSubscriptionUseCase: UpdateRaidSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateRaidSubscriptionRequest,
    ): CreateRaidSubscriptionResponse {
        val userId = UserId(currentMnemosUser.requireUserId())

        return createRaidSubscriptionUseCase.execute(request.toCommand(userId)).toResponse()
    }

    @PatchMapping
    fun update(
        @Valid @RequestBody request: CreateRaidSubscriptionRequest,
    ): CreateRaidSubscriptionResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        val result =
            updateRaidSubscriptionUseCase.execute(
                UpdateRaidSubscriptionCommand(
                    userId = userId,
                    locations = request.locations,
                ),
            )

        return CreateRaidSubscriptionResponse(
            subscriptionId = result.subscriptionId.id,
        )
    }
}
