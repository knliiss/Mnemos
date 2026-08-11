package com.mnemos.notification.adapter.inbound.http.itemdrop

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.itemdrop.create.CreateItemDropSubscriptionApiMapper.toCommand
import com.mnemos.notification.adapter.inbound.http.itemdrop.create.CreateItemDropSubscriptionApiMapper.toResponse
import com.mnemos.notification.adapter.inbound.http.itemdrop.create.CreateItemDropSubscriptionRequest
import com.mnemos.notification.adapter.inbound.http.itemdrop.create.CreateItemDropSubscriptionResponse
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionUseCase
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions/item-drops")
class ItemDropSubscriptionController(
    private val createItemDropSubscriptionUseCase: CreateItemDropSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateItemDropSubscriptionRequest,
    ): CreateItemDropSubscriptionResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        return createItemDropSubscriptionUseCase.execute(request.toCommand(userId)).toResponse()
    }
}
