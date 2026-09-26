package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.notification.application.itemdrop.internal.InternalDropArbitrationService
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventCommand
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@Validated
@RestController
@RequestMapping("/internal/v1/crystalrun/item-drops")
@PreAuthorize("authentication.name == 'crystalrun'")
class CrystalRunInternalDropController(
    private val arbitrationService: InternalDropArbitrationService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun submit(
        @Valid @RequestBody request: CrystalRunInternalDropRequest,
    ) {
        arbitrationService.submitInternal(request.toCommand())
    }

    private fun CrystalRunInternalDropRequest.toCommand(): ProcessItemDropEventCommand =
        ProcessItemDropEventCommand(
            messageId = messageId,
            occurredAt = observedAt,
            droppedFor = droppedFor,
            itemKey = itemKey,
            itemName = itemName,
            itemRarity = itemRarity,
            itemType = itemType,
        )
}

data class CrystalRunInternalDropRequest(
    val messageId: UUID,
    val observedAt: Instant,
    @field:Size(max = 160)
    @field:Pattern(regexp = "^sao\\.item\\.[A-Za-z0-9_.-]+\\.name$")
    val itemKey: String? = null,
    @field:NotBlank
    @field:Size(min = 1, max = 96)
    val itemName: String,
    val itemType: NotificationItemType,
    val itemRarity: NotificationItemRarity,
    @field:NotBlank
    @field:Size(min = 4, max = 20)
    @field:Pattern(regexp = "^[\\p{L}0-9_]+$")
    val droppedFor: String,
)
