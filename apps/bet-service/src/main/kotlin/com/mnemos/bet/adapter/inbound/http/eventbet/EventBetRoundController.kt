package com.mnemos.bet.adapter.inbound.http.eventbet

import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetApiMapper.toResponse
import com.mnemos.bet.application.eventbet.usecase.get.GetEventBetRoundQuery
import com.mnemos.bet.application.eventbet.usecase.get.GetEventBetRoundUseCase
import com.mnemos.bet.application.eventbet.usecase.list.ListEventBetRoundsUseCase
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/event-rounds")
class EventBetRoundController(
    private val listEventBetRoundsUseCase: ListEventBetRoundsUseCase,
    private val getEventBetRoundUseCase: GetEventBetRoundUseCase,
) {
    @GetMapping
    fun list(): List<EventBetRoundResponse> = listEventBetRoundsUseCase.execute().map { it.toResponse() }

    @GetMapping("/{roundId}")
    fun get(
        @PathVariable
        roundId: UUID,
    ): EventBetRoundResponse =
        getEventBetRoundUseCase
            .execute(
                GetEventBetRoundQuery(
                    roundId = EventBetRoundId(roundId),
                ),
            ).toResponse()
}
