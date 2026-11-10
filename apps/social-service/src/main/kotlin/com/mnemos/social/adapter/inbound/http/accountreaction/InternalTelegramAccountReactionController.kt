package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.application.actor.ResolveTelegramSocialActorUseCase
import com.mnemos.social.application.accountreaction.usecase.get.GetAccountReactionForActorUseCase
import com.mnemos.social.application.accountreaction.usecase.remove.RemoveAccountReactionForActorUseCase
import com.mnemos.social.application.accountreaction.usecase.set.SetAccountReactionCommand
import com.mnemos.social.application.accountreaction.usecase.set.SetAccountReactionForActorUseCase
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionState
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionType
import com.mnemos.social.domain.reaction.model.AccountReactionType
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/telegram/social/users/{telegramId}/profiles/{accountId}/reaction")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramAccountReactionController(
    private val actors: ResolveTelegramSocialActorUseCase,
    private val getReaction: GetAccountReactionForActorUseCase,
    private val setReaction: SetAccountReactionForActorUseCase,
    private val removeReaction: RemoveAccountReactionForActorUseCase,
) {
    @GetMapping
    fun current(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
    ): InternalTelegramReactionState {
        val actor = actors.resolve(telegramId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val reaction = getReaction.executeForActor(accountId, actor)
        return InternalTelegramReactionState(reaction?.let { InternalTelegramReactionType.valueOf(it.name) })
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun set(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
        @RequestBody request: InternalTelegramReactionRequest,
    ) {
        val actor = actors.resolve(telegramId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        setReaction.executeForActor(
            SetAccountReactionCommand(accountId, AccountReactionType.valueOf(request.type.name)),
            actor,
        )
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(
        @PathVariable telegramId: Long,
        @PathVariable accountId: UUID,
    ) {
        val actor = actors.resolve(telegramId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        removeReaction.executeForActor(accountId, actor)
    }
}
