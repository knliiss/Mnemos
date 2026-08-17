package com.mnemos.bet.adapter.inbound.http.itemrace

import com.mnemos.bet.adapter.inbound.http.common.AcceptedBetOperationResponse
import com.mnemos.bet.adapter.inbound.http.itemrace.ItemRaceApiMapper.toResponse
import com.mnemos.bet.application.itemrace.usecase.history.ListMyItemRaceHistoryUseCase
import com.mnemos.bet.application.itemrace.usecase.increase.IncreaseItemRaceBetCommand
import com.mnemos.bet.application.itemrace.usecase.increase.IncreaseItemRaceBetUseCase
import com.mnemos.bet.application.itemrace.usecase.join.JoinItemRaceCommand
import com.mnemos.bet.application.itemrace.usecase.join.JoinItemRaceUseCase
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.common.web.logging.MnemosHeaders.IDEMPOTENCY_KEY
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/item-race-participants")
class ItemRaceParticipantController(
    private val joinItemRaceUseCase: JoinItemRaceUseCase,
    private val increaseItemRaceBetUseCase: IncreaseItemRaceBetUseCase,
    private val listMyItemRaceHistoryUseCase: ListMyItemRaceHistoryUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun join(
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @RequestBody
        @Valid
        request: JoinItemRaceRequest,
    ): AcceptedBetOperationResponse {
        val result =
            joinItemRaceUseCase.execute(
                JoinItemRaceCommand(
                    idempotencyKey = idempotencyKey,
                    raceId = ItemRaceId(request.raceId),
                ),
            )

        return AcceptedBetOperationResponse(
            resourceId = result.participantId.id,
            operationId = result.operationId.id,
        )
    }

    @PatchMapping("/increase")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun increase(
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @RequestBody
        @Valid
        request: IncreaseItemRaceBetRequest,
    ): AcceptedBetOperationResponse {
        val result =
            increaseItemRaceBetUseCase.execute(
                IncreaseItemRaceBetCommand(
                    idempotencyKey = idempotencyKey,
                    raceId = ItemRaceId(request.raceId),
                    amount = request.amount,
                ),
            )

        return AcceptedBetOperationResponse(
            resourceId = result.participantId.id,
            operationId = result.operationId.id,
        )
    }

    @GetMapping("/me")
    fun history(): List<ItemRaceParticipantResponse> = listMyItemRaceHistoryUseCase.execute().map { it.toResponse() }
}
