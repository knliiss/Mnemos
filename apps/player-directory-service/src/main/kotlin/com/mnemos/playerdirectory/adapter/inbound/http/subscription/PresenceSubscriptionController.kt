package com.mnemos.playerdirectory.adapter.inbound.http.subscription

import com.mnemos.playerdirectory.adapter.inbound.http.subscription.PresenceSubscriptionApiMapper.toResponse
import com.mnemos.playerdirectory.application.subscription.usecase.create.CreatePresenceSubscriptionCommand
import com.mnemos.playerdirectory.application.subscription.usecase.create.CreatePresenceSubscriptionUseCase
import com.mnemos.playerdirectory.application.subscription.usecase.delete.DeletePresenceSubscriptionUseCase
import com.mnemos.playerdirectory.application.subscription.usecase.list.ListPresenceSubscriptionsUseCase
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.security.CurrentMnemosUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/player-presence-subscriptions")
class PresenceSubscriptionController(
    private val createUseCase: CreatePresenceSubscriptionUseCase,
    private val listUseCase: ListPresenceSubscriptionsUseCase,
    private val deleteUseCase: DeletePresenceSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: CreatePresenceSubscriptionRequest,
    ): PresenceSubscriptionResponse {
        val subscriberUserId = currentMnemosUser.requireUserId()
        val subscription =
            createUseCase.execute(
                CreatePresenceSubscriptionCommand(
                    subscriberUserId = subscriberUserId,
                    playerId = CristalixPlayerId(request.playerId),
                ),
            )

        return subscription.toResponse()
    }

    @GetMapping
    fun list(): List<PresenceSubscriptionResponse> {
        val subscriberUserId = currentMnemosUser.requireUserId()

        return listUseCase
            .execute(subscriberUserId)
            .map { subscription -> subscription.toResponse() }
    }

    @DeleteMapping("/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable playerId: UUID,
    ) {
        deleteUseCase.execute(
            subscriberUserId = currentMnemosUser.requireUserId(),
            playerId = CristalixPlayerId(playerId),
        )
    }
}
