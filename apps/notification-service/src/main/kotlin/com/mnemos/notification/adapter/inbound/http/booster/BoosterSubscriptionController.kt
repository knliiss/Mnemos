package com.mnemos.notification.adapter.inbound.http.booster

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.booster.create.CreateBoosterSubscriptionApiMapper.toCommand
import com.mnemos.notification.adapter.inbound.http.booster.create.CreateBoosterSubscriptionApiMapper.toResponse
import com.mnemos.notification.adapter.inbound.http.booster.create.CreateBoosterSubscriptionRequest
import com.mnemos.notification.adapter.inbound.http.booster.create.CreateBoosterSubscriptionResponse
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionUseCase
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions/boosters")
class BoosterSubscriptionController(
    private val createBoosterSubscriptionUseCase: CreateBoosterSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateBoosterSubscriptionRequest,
    ): CreateBoosterSubscriptionResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        return createBoosterSubscriptionUseCase.execute(request.toCommand(userId)).toResponse()
    }
}
