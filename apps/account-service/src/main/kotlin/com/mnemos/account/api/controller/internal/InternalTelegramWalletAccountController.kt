package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalTelegramActiveAccountApiMapper.toActiveAccountResponse
import com.mnemos.account.application.service.user.InternalTelegramUserQueryService
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.contracts.account.internal.v1.InternalTelegramActiveAccountResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramWalletContextResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/users/telegram")
@PreAuthorize("authentication.name == 'wallet-service'")
class InternalTelegramWalletAccountController(
    private val service: InternalTelegramUserQueryService,
) {
    @GetMapping("/{telegramId}/active-account")
    fun getActiveAccount(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramActiveAccountResponse> {
        val context =
            service.findByTelegramId(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(context.toActiveAccountResponse())
    }
    @GetMapping("/{telegramId}/wallet-context")
    fun getWalletContext(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramWalletContextResponse> {
        val context =
            service.findByTelegramId(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalTelegramWalletContextResponse(
                userId = context.userId.id,
                activeAccountId = context.activeAccountId?.id,
            ),
        )
    }

}
