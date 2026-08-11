package com.mnemos.notification.adapter.inbound.http.dungeon

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.dungeon.create.CreateDungeonSubscriptionApiMapper.toCommand
import com.mnemos.notification.adapter.inbound.http.dungeon.create.CreateDungeonSubscriptionApiMapper.toResponse
import com.mnemos.notification.adapter.inbound.http.dungeon.create.CreateDungeonSubscriptionRequest
import com.mnemos.notification.adapter.inbound.http.dungeon.create.CreateDungeonSubscriptionResponse
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionUseCase
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/subscriptions/dungeons")
class DungeonSubscriptionController(
    private val createDungeonSubscriptionUseCase: CreateDungeonSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateDungeonSubscriptionRequest,
    ): CreateDungeonSubscriptionResponse {
        val userId = UserId(currentMnemosUser.requireUserId())
        return createDungeonSubscriptionUseCase.execute(request.toCommand(userId)).toResponse()
    }
}
