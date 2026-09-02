package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.adapter.inbound.http.internal.notificationpack.InternalTelegramNotificationPackApiMapper.toResponse
import com.mnemos.personalization.application.ownership.usecase.notificationpack.TelegramNotificationPackManagementService
import com.mnemos.personalization.application.ownership.usecase.notificationpack.TelegramNotificationPackPurchaseService
import com.mnemos.personalization.application.ownership.usecase.notificationpack.TelegramNotificationPackStoreService
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackListResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackPurchaseResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackStoreItemResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackStoreResponse
import com.mnemos.personalization.contracts.internal.v1.PurchaseInternalTelegramNotificationPackRequest
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/users/telegram/{telegramId}/notification-packs")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramNotificationPackController(
    private val managementService: TelegramNotificationPackManagementService,
    private val storeService: TelegramNotificationPackStoreService,
    private val purchaseService: TelegramNotificationPackPurchaseService,
) {
    @GetMapping
    fun list(
        @PathVariable telegramId: Long,
    ): ResponseEntity<InternalTelegramNotificationPackListResponse> {
        val packs =
            managementService.listOwned(TelegramId(telegramId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalTelegramNotificationPackListResponse(
                packs = packs.map { pack -> pack.toResponse() },
            ),
        )
    }

    @GetMapping("/store")
    fun store(
        @PathVariable telegramId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "6") size: Int,
    ): ResponseEntity<InternalTelegramNotificationPackStoreResponse> {
        val result =
            storeService.list(
                telegramId = TelegramId(telegramId),
                page = page,
                size = size,
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalTelegramNotificationPackStoreResponse(
                packs =
                    result.items.map { item ->
                        InternalTelegramNotificationPackStoreItemResponse(
                            personalizationId = item.personalizationId.id,
                            name = item.name,
                            author = item.author,
                            priceAmount = item.priceAmount,
                            currency = item.currency.name,
                            owned = item.owned,
                            active = item.active,
                            samples = item.samples,
                        )
                    },
                page = result.page,
                totalItems = result.totalItems,
                totalPages = result.totalPages,
                hasNextPage = result.hasNextPage,
            ),
        )
    }

    @PostMapping("/store/{personalizationId}/purchase")
    fun purchase(
        @PathVariable telegramId: Long,
        @PathVariable personalizationId: UUID,
        @RequestBody request: PurchaseInternalTelegramNotificationPackRequest,
    ): ResponseEntity<InternalTelegramNotificationPackPurchaseResponse> {
        val result =
            purchaseService.purchase(
                telegramId = TelegramId(telegramId),
                personalizationId = PersonalizationId(personalizationId),
                idempotencyKey = request.idempotencyKey,
            ) ?: return ResponseEntity.notFound().build()

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(
                InternalTelegramNotificationPackPurchaseResponse(
                    personalizationId = result.personalizationId.id,
                    operationId = result.operationId.id,
                ),
            )
    }
}
