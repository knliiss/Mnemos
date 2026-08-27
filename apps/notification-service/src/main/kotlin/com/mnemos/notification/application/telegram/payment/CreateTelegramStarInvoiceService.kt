package com.mnemos.notification.application.telegram.payment

import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceRequest
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.LabeledPrice
import com.pengrad.telegrambot.request.CreateInvoiceLink
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class CreateTelegramStarInvoiceService(
    private val bot: TelegramBot,
) {
    fun create(request: CreateStarInvoiceRequest): String {
        if (request.starsAmount <= 0) {
            throw AppException(
                message = "Telegram Stars invoice amount must be positive",
                code = "STAR_INVOICE_AMOUNT_INVALID",
                httpStatus = HttpStatus.BAD_REQUEST,
            )
        }

        val invoiceRequest =
            CreateInvoiceLink(
                request.title,
                request.description,
                request.payload,
                STARS_CURRENCY,
                LabeledPrice(request.title, request.starsAmount),
            )

        request.photoUrl
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let(invoiceRequest::photoUrl)

        val response = bot.execute(invoiceRequest)

        if (!response.isOk) {
            throw AppException(
                message = response.description() ?: "Telegram failed to create Stars invoice",
                code = "TELEGRAM_STAR_INVOICE_FAILED",
                httpStatus = HttpStatus.BAD_GATEWAY,
                details = mapOf("telegramErrorCode" to response.errorCode()),
            )
        }

        return response
            .result()
            ?.takeIf(String::isNotBlank)
            ?: throw AppException(
                message = "Telegram returned an empty Stars invoice URL",
                code = "TELEGRAM_STAR_INVOICE_EMPTY",
                httpStatus = HttpStatus.BAD_GATEWAY,
            )
    }

    private companion object {
        const val STARS_CURRENCY = "XTR"
    }
}
