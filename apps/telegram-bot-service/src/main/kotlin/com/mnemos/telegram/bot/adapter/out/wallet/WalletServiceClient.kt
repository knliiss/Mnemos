package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "telegram-bot-wallet-service-client",
    url = "\${mnemos.clients.wallet-service.url}",
    configuration = [WalletServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface WalletServiceClient {
    @GetMapping("/internal/v1/telegram/users/{telegramId}/active-account/balances")
    fun getActiveAccountBalances(
        @PathVariable telegramId: Long,
    ): List<TelegramAccountBalanceResponse>?
}
