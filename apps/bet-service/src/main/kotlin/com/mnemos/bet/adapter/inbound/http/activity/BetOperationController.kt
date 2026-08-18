package com.mnemos.bet.adapter.inbound.http.activity

import com.mnemos.bet.adapter.inbound.http.activity.BetOperationApiMapper.toResponse
import com.mnemos.bet.application.activity.usecase.GetBetOperationService
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/operations")
class BetOperationController(
    private val service: GetBetOperationService,
) {
    @GetMapping("/{operationId}")
    fun get(
        @PathVariable operationId: UUID,
    ): BetOperationResponse = service.execute(WalletOperationId(operationId)).toResponse()
}
