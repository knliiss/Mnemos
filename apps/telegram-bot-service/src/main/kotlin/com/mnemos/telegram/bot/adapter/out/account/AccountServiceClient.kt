package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "telegram-bot-account-service-client",
    url = "\${mnemos.clients.account-service.url}",
    configuration = [AccountServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface AccountServiceClient {
    @GetMapping("/internal/v1/users/telegram/{telegramId}/accounts")
    fun getAccounts(
        @PathVariable telegramId: Long,
    ): InternalTelegramAccountContextResponse?

    @PutMapping("/internal/v1/users/telegram/{telegramId}/active-account")
    fun changeActiveAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: ChangeInternalTelegramActiveAccountRequest,
    ): InternalTelegramAccountContextResponse?
}
