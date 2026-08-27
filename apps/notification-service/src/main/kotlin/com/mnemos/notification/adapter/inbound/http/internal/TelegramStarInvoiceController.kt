package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.notification.application.telegram.payment.CreateTelegramStarInvoiceService
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceRequest
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceResponse
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/telegram/stars")
@PreAuthorize("authentication.name == 'payment-service'")
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class TelegramStarInvoiceController(
    private val service: CreateTelegramStarInvoiceService,
) {
    @PostMapping("/invoices")
    fun createInvoice(
        @Valid @RequestBody request: CreateStarInvoiceRequest,
    ): CreateStarInvoiceResponse =
        CreateStarInvoiceResponse(
            invoiceUrl = service.create(request),
        )
}
