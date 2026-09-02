package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteResponse
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInvitesResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserSettingsResponse
import com.mnemos.contracts.account.internal.v1.JoinInternalTelegramAccountRequest
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramUserLocalizationRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

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

    @PostMapping("/internal/v1/users/telegram/{telegramId}/accounts")
    fun createAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: CreateInternalTelegramAccountRequest,
    ): InternalTelegramAccountContextResponse?

    @PostMapping("/internal/v1/users/telegram/{telegramId}/accounts/join")
    fun joinAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: JoinInternalTelegramAccountRequest,
    ): InternalTelegramAccountContextResponse?

    @PutMapping("/internal/v1/users/telegram/{telegramId}/active-account")
    fun changeActiveAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: ChangeInternalTelegramActiveAccountRequest,
    ): InternalTelegramAccountContextResponse?

    @GetMapping("/internal/v1/users/telegram/{telegramId}/active-account/invites")
    fun getAccountInvites(
        @PathVariable telegramId: Long,
    ): InternalTelegramAccountInvitesResponse?

    @PostMapping("/internal/v1/users/telegram/{telegramId}/active-account/invites")
    fun createAccountInvite(
        @PathVariable telegramId: Long,
        @RequestBody request: CreateInternalTelegramAccountInviteRequest,
    ): CreateInternalTelegramAccountInviteResponse?

    @DeleteMapping("/internal/v1/users/telegram/{telegramId}/active-account/invites/{inviteId}")
    fun revokeAccountInvite(
        @PathVariable telegramId: Long,
        @PathVariable inviteId: UUID,
    ): ResponseEntity<Void>?

    @GetMapping("/internal/v1/users/telegram/{telegramId}/settings")
    fun getUserSettings(
        @PathVariable telegramId: Long,
    ): InternalTelegramUserSettingsResponse?

    @PutMapping("/internal/v1/users/telegram/{telegramId}/settings/localization")
    fun updateLocalization(
        @PathVariable telegramId: Long,
        @RequestBody request: UpdateInternalTelegramUserLocalizationRequest,
    ): InternalTelegramUserSettingsResponse?
}
