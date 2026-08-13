package com.mnemos.bet.adapter.inbound.http.eventbet

import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetApiMapper.toResponse
import com.mnemos.bet.application.eventbet.usecase.history.ListMyEventBetsUseCase
import com.mnemos.bet.application.eventbet.usecase.increase.IncreaseEventBetCommand
import com.mnemos.bet.application.eventbet.usecase.increase.IncreaseEventBetUseCase
import com.mnemos.bet.application.eventbet.usecase.place.PlaceEventBetCommand
import com.mnemos.bet.application.eventbet.usecase.place.PlaceEventBetUseCase
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/event-bets")
class EventBetController(
    private val placeEventBetUseCase: PlaceEventBetUseCase,
    private val increaseEventBetUseCase: IncreaseEventBetUseCase,
    private val listMyEventBetsUseCase: ListMyEventBetsUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun place(
        @RequestBody
        @Valid
        request: PlaceEventBetRequest,
    ): UUID =
        placeEventBetUseCase
            .execute(
                PlaceEventBetCommand(
                    roundId = EventBetRoundId(request.roundId),
                    side = DomainBetSide.valueOf(request.side.name),
                    amount = request.amount,
                ),
            ).betId
            .id

    @PatchMapping("/increase")
    fun increase(
        @RequestBody
        @Valid
        request: IncreaseEventBetRequest,
    ): UUID =
        increaseEventBetUseCase
            .execute(
                IncreaseEventBetCommand(
                    roundId = EventBetRoundId(request.roundId),
                    amount = request.amount,
                ),
            ).betId
            .id

    @GetMapping("/me")
    fun history(): List<EventBetResponse> = listMyEventBetsUseCase.execute().map { it.toResponse() }
}
