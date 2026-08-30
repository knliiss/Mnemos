package com.mnemos.telegram.bot.adapter.inbound.http.internal

import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceRequest
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceResponse
import com.mnemos.telegram.bot.application.payment.CreateTelegramStarInvoiceService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/stars")
@PreAuthorize("authentication.name == 'payment-service'")
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramStarInvoiceController(
    private val service: CreateTelegramStarInvoiceService,
) {
    @PostMapping("/invoices")
    fun createInvoice(
        @RequestBody request: CreateStarInvoiceRequest,
    ): CreateStarInvoiceResponse =
        CreateStarInvoiceResponse(
            invoiceUrl = service.create(request),
        )
}
