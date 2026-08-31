package com.mnemos.bet.adapter.inbound.http.internal

import com.mnemos.bet.adapter.inbound.http.internal.TelegramEventBetInternalApiMapper.toTelegramResponse
import com.mnemos.bet.adapter.inbound.http.internal.TelegramItemRaceInternalApiMapper.toTelegramResponse
import com.mnemos.bet.application.eventbet.usecase.history.ListMyEventBetsService
import com.mnemos.bet.application.eventbet.usecase.increase.IncreaseEventBetCommand
import com.mnemos.bet.application.eventbet.usecase.increase.IncreaseEventBetService
import com.mnemos.bet.application.eventbet.usecase.list.ListEventBetRoundsService
import com.mnemos.bet.application.eventbet.usecase.place.PlaceEventBetCommand
import com.mnemos.bet.application.eventbet.usecase.place.PlaceEventBetService
import com.mnemos.bet.application.itemrace.usecase.history.ListMyItemRaceHistoryService
import com.mnemos.bet.application.itemrace.usecase.increase.IncreaseItemRaceBetCommand
import com.mnemos.bet.application.itemrace.usecase.increase.IncreaseItemRaceBetService
import com.mnemos.bet.application.itemrace.usecase.join.JoinItemRaceCommand
import com.mnemos.bet.application.itemrace.usecase.join.JoinItemRaceService
import com.mnemos.bet.application.itemrace.usecase.list.ListItemRacesService
import com.mnemos.bet.application.security.port.out.TelegramBetActorProvider
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramBetOperationResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetRoundResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramIncreaseEventBetRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramIncreaseItemRaceRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceParticipantResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramJoinItemRaceRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramPlaceEventBetRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/telegram/bets")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramEventBetInternalController(
    private val actorProvider: TelegramBetActorProvider,
    private val listRoundsService: ListEventBetRoundsService,
    private val eventHistoryService: ListMyEventBetsService,
    private val placeService: PlaceEventBetService,
    private val increaseService: IncreaseEventBetService,
    private val listItemRacesService: ListItemRacesService,
    private val itemRaceHistoryService: ListMyItemRaceHistoryService,
    private val joinItemRaceService: JoinItemRaceService,
    private val increaseItemRaceBetService: IncreaseItemRaceBetService,
) {
    @GetMapping("/{telegramId}/event-rounds")
    fun listRounds(
        @PathVariable telegramId: Long,
    ): List<TelegramEventBetRoundResponse> {
        val actorUserId = requireActor(telegramId)

        return listRoundsService.executeFor(actorUserId).map { round -> round.toTelegramResponse() }
    }

    @GetMapping("/{telegramId}/event-bets/history")
    fun eventHistory(
        @PathVariable telegramId: Long,
    ): List<TelegramEventBetResponse> {
        val actorUserId = requireActor(telegramId)

        return eventHistoryService.executeFor(actorUserId).map { bet -> bet.toTelegramResponse() }
    }

    @PostMapping("/{telegramId}/event-rounds/{roundId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun place(
        @PathVariable telegramId: Long,
        @PathVariable roundId: UUID,
        @RequestBody request: TelegramPlaceEventBetRequest,
    ): TelegramBetOperationResponse {
        val actorUserId = requireActor(telegramId)
        val result =
            placeService.executeFor(
                command =
                    PlaceEventBetCommand(
                        roundId = EventBetRoundId(roundId),
                        side = DomainBetSide.valueOf(request.side.name),
                        amount = request.amount,
                        idempotencyKey = request.idempotencyKey,
                    ),
                actorUserId = actorUserId,
            )

        return TelegramBetOperationResponse(
            resourceId = result.betId.id,
            operationId = result.operationId.id,
        )
    }

    @PatchMapping("/{telegramId}/event-rounds/{roundId}/increase")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun increase(
        @PathVariable telegramId: Long,
        @PathVariable roundId: UUID,
        @RequestBody request: TelegramIncreaseEventBetRequest,
    ): TelegramBetOperationResponse {
        val actorUserId = requireActor(telegramId)
        val result =
            increaseService.executeFor(
                command =
                    IncreaseEventBetCommand(
                        roundId = EventBetRoundId(roundId),
                        amount = request.amount,
                        idempotencyKey = request.idempotencyKey,
                    ),
                actorUserId = actorUserId,
            )

        return TelegramBetOperationResponse(
            resourceId = result.betId.id,
            operationId = result.operationId.id,
        )
    }

    @GetMapping("/{telegramId}/item-races")
    fun listItemRaces(
        @PathVariable telegramId: Long,
    ): List<TelegramItemRaceResponse> {
        val actorUserId = requireActor(telegramId)

        return listItemRacesService.executeFor(actorUserId).map { race -> race.toTelegramResponse() }
    }

    @GetMapping("/{telegramId}/item-races/history")
    fun itemRaceHistory(
        @PathVariable telegramId: Long,
    ): List<TelegramItemRaceParticipantResponse> {
        val actorUserId = requireActor(telegramId)

        return itemRaceHistoryService.executeFor(actorUserId).map { position -> position.toTelegramResponse() }
    }

    @PostMapping("/{telegramId}/item-races/{raceId}/join")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun joinItemRace(
        @PathVariable telegramId: Long,
        @PathVariable raceId: UUID,
        @RequestBody request: TelegramJoinItemRaceRequest,
    ): TelegramBetOperationResponse {
        val actorUserId = requireActor(telegramId)
        val result =
            joinItemRaceService.executeFor(
                command =
                    JoinItemRaceCommand(
                        raceId = ItemRaceId(raceId),
                        idempotencyKey = request.idempotencyKey,
                    ),
                actorUserId = actorUserId,
            )

        return TelegramBetOperationResponse(
            resourceId = result.participantId.id,
            operationId = result.operationId.id,
        )
    }

    @PatchMapping("/{telegramId}/item-races/{raceId}/increase")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun increaseItemRace(
        @PathVariable telegramId: Long,
        @PathVariable raceId: UUID,
        @RequestBody request: TelegramIncreaseItemRaceRequest,
    ): TelegramBetOperationResponse {
        val actorUserId = requireActor(telegramId)
        val result =
            increaseItemRaceBetService.executeFor(
                command =
                    IncreaseItemRaceBetCommand(
                        raceId = ItemRaceId(raceId),
                        amount = request.amount,
                        idempotencyKey = request.idempotencyKey,
                    ),
                actorUserId = actorUserId,
            )

        return TelegramBetOperationResponse(
            resourceId = result.participantId.id,
            operationId = result.operationId.id,
        )
    }

    private fun requireActor(telegramId: Long): UserId =
        actorProvider.findUserId(TelegramId(telegramId))
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Telegram user is not registered in Mnemos",
            )
}
