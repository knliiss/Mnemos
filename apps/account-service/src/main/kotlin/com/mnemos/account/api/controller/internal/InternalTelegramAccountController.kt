package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramAccountApiMapper.toResponse
import com.mnemos.account.application.service.account.InternalTelegramAccountService
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import com.mnemos.contracts.account.internal.v1.JoinInternalTelegramAccountRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramAccountController(
    private val service: InternalTelegramAccountService,
) {
    @GetMapping("/{telegramId}/accounts")
    fun getAccounts(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramAccountContextResponse> {
        val context =
            service.getContext(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(context.toResponse())
    }

    @PostMapping("/{telegramId}/accounts")
    fun createAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: CreateInternalTelegramAccountRequest,
    ): InternalTelegramAccountContextResponse =
        service
            .createAccount(
                telegramId = telegramId,
                cristalixNickname = request.cristalixNickname,
            ).toResponse()

    @PostMapping("/{telegramId}/accounts/join")
    fun joinAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: JoinInternalTelegramAccountRequest,
    ): InternalTelegramAccountContextResponse =
        service
            .joinAccount(
                telegramId = telegramId,
                inviteCode = request.inviteCode,
            ).toResponse()

    @PutMapping("/{telegramId}/active-account")
    fun changeActiveAccount(
        @PathVariable telegramId: Long,
        @RequestBody request: ChangeInternalTelegramActiveAccountRequest,
    ): ResponseEntity<InternalTelegramAccountContextResponse> {
        val context =
            service.changeActiveAccount(
                telegramId = TelegramId(telegramId),
                accountId = AccountId(request.accountId),
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(context.toResponse())
    }
}
