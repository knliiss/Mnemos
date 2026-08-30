package com.mnemos.wallet.adapter.inbound.http.internal

import com.mnemos.wallet.adapter.inbound.http.internal.TelegramAccountBalanceApiMapper.toTelegramResponse
import com.mnemos.wallet.application.balance.usecase.get.GetTelegramAccountBalancesService
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/users")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramAccountBalanceController(
    private val service: GetTelegramAccountBalancesService,
) {
    @GetMapping("/{telegramId}/active-account/balances")
    fun balances(
        @PathVariable telegramId: Long,
    ): ResponseEntity<List<TelegramAccountBalanceResponse>> {
        val balances = service.execute(telegramId) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            balances.map { balance -> balance.toTelegramResponse() },
        )
    }
}
