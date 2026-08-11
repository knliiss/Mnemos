package com.mnemos.notification.adapter.inbound.http.global

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.global.create.CreateGlobalSubscriptionApiMapper.toCommand
import com.mnemos.notification.adapter.inbound.http.global.create.CreateGlobalSubscriptionApiMapper.toResponse
import com.mnemos.notification.adapter.inbound.http.global.create.CreateGlobalSubscriptionRequest
import com.mnemos.notification.adapter.inbound.http.global.create.CreateGlobalSubscriptionResponse
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionUseCase
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions/global-events")
class GlobalSubscriptionController(
    private val createGlobalSubscriptionUseCase: CreateGlobalSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateGlobalSubscriptionRequest,
    ): CreateGlobalSubscriptionResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        return createGlobalSubscriptionUseCase.execute(request.toCommand(userId)).toResponse()
    }
}
