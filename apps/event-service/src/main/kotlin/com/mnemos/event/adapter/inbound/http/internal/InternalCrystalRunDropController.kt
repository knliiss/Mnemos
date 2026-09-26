package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.internaldrop.InternalDropFallbackService
import com.mnemos.event.application.internaldrop.SubmitInternalDropCommand
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/crystalrun/item-drops")
@PreAuthorize("authentication.name == 'crystalrun'")
class InternalCrystalRunDropController(
    private val service: InternalDropFallbackService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun submit(@Valid @RequestBody request: InternalCrystalRunDropRequest) {
        service.submit(
            SubmitInternalDropCommand(
                messageId = request.messageId,
                observedAt = request.observedAt,
                itemKey = request.itemKey,
                itemName = request.itemName,
                itemType = request.itemType,
                itemRarity = request.itemRarity,
                droppedFor = request.droppedFor,
            ),
        )
    }
}

data class InternalCrystalRunDropRequest(
    val messageId: UUID,
    val observedAt: Instant,
    @field:Size(max = 160)
    @field:Pattern(regexp = "^sao\\.item\\.[A-Za-z0-9_.-]+\\.name$")
    val itemKey: String? = null,
    @field:NotBlank
    @field:Size(min = 1, max = 96)
    val itemName: String,
    val itemType: DomainItemType,
    val itemRarity: DomainItemRarity,
    @field:NotBlank
    @field:Size(min = 4, max = 20)
    @field:Pattern(regexp = "^[\\p{L}0-9_]+$")
    val droppedFor: String,
)
