package com.mnemos.wallet.adapter.inbound.http.internal

import com.mnemos.wallet.application.common.pagination.PageQuery
import com.mnemos.wallet.application.transaction.usecase.list.GetTelegramAccountTransactionsService
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountTransactionEntry
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountTransactionsResponse
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/users")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
@Validated
class TelegramAccountTransactionController(
    private val service: GetTelegramAccountTransactionsService,
) {
    @GetMapping("/{telegramId}/active-account/transactions")
    fun transactions(
        @PathVariable telegramId: Long,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "4") @Min(1) @Max(4) size: Int,
    ): ResponseEntity<TelegramAccountTransactionsResponse> {
        val result = service.execute(telegramId, PageQuery(page, size))
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            TelegramAccountTransactionsResponse(
                accountId = result.accountId.id,
                items =
                    result.page.items.map { entry ->
                        TelegramAccountTransactionEntry(
                            id = entry.id,
                            currency = entry.currency,
                            type = entry.type,
                            source = entry.source,
                            amount = entry.amount,
                            createdAt = entry.createdAt,
                        )
                    },
                page = result.page.page,
                totalPages = result.page.totalPages,
                totalItems = result.page.totalItems,
            ),
        )
    }
}
