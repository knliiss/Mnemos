package com.mnemos.wallet.adapter.inbound.http.internal

import com.mnemos.wallet.adapter.inbound.http.internal.TelegramAccountBalanceApiMapper.toTelegramResponse
import com.mnemos.wallet.application.balance.usecase.get.GetWalletBalancesService
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/telegram/accounts")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class TelegramAccountBalanceController(
    private val getWalletBalancesService: GetWalletBalancesService,
) {
    @GetMapping("/{accountId}/balances")
    fun balances(
        @PathVariable accountId: UUID,
    ): List<TelegramAccountBalanceResponse> {
        val balances =
            getWalletBalancesService.execute(
                ownerType = WalletOwnerType.ACCOUNT,
                ownerId = accountId,
            )

        return balances.map { balance -> balance.toTelegramResponse() }
    }
}
