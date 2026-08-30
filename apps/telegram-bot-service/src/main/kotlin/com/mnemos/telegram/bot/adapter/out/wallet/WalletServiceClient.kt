package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@FeignClient(
    name = "telegram-bot-wallet-service-client",
    url = "\${mnemos.clients.wallet-service.url}",
    configuration = [WalletServiceFeignConfiguration::class],
)
interface WalletServiceClient {
    @GetMapping("/internal/v1/telegram/accounts/{accountId}/balances")
    fun getAccountBalances(
        @PathVariable accountId: UUID,
    ): List<TelegramAccountBalanceResponse>
}
