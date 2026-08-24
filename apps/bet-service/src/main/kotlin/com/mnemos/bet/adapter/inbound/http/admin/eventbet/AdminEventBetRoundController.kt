package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import com.mnemos.bet.adapter.inbound.http.admin.eventbet.AdminEventBetApiMapper.toResponse
import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetApiMapper.toResponse
import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetRoundResponse
import com.mnemos.bet.application.eventbet.usecase.adminbets.ListAdminEventBetsUseCase
import com.mnemos.bet.application.eventbet.usecase.adminlist.ListAdminEventBetRoundsUseCase
import com.mnemos.bet.application.eventbet.usecase.resolve.ResolveEventBetRoundCommand
import com.mnemos.bet.application.eventbet.usecase.resolve.ResolveEventBetRoundUseCase
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/admin/event-rounds")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminEventBetRoundController(
    private val listAdminEventBetRoundsUseCase: ListAdminEventBetRoundsUseCase,
    private val listAdminEventBetsUseCase: ListAdminEventBetsUseCase,
    private val resolveEventBetRoundUseCase: ResolveEventBetRoundUseCase,
) {
    @GetMapping
    fun list(): List<EventBetRoundResponse> = listAdminEventBetRoundsUseCase.execute().map { it.toResponse() }

    @GetMapping("/{roundId}/bets")
    fun listBets(
        @PathVariable
        roundId: UUID,
    ): List<AdminEventBetResponse> =
        listAdminEventBetsUseCase
            .execute(EventBetRoundId(roundId))
            .map { bet -> bet.toResponse() }

    @PostMapping("/{roundId}/resolution")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resolve(
        @PathVariable
        roundId: UUID,
        @RequestBody
        @Valid
        request: ResolveEventBetRoundRequest,
    ) {
        resolveEventBetRoundUseCase.execute(
            ResolveEventBetRoundCommand(
                roundId = EventBetRoundId(roundId),
                outcome = request.outcome,
                reason = request.reason,
            ),
        )
    }
}
