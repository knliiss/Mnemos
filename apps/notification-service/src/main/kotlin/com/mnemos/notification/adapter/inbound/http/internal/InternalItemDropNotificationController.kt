package com.mnemos.notification.adapter.inbound.http.internal

import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventCommand
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventUseCase
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/item-drop-notifications")
@PreAuthorize("authentication.name == 'event-service'")
class InternalItemDropNotificationController(
    private val processItemDropEventUseCase: ProcessItemDropEventUseCase,
) {
    @PostMapping
    fun notify(@RequestBody request: InternalItemDropNotificationRequest) {
        processItemDropEventUseCase.execute(
            ProcessItemDropEventCommand(
                messageId = request.sourceId,
                occurredAt = request.occurredAt,
                droppedFor = request.droppedFor,
                itemKey = request.itemKey,
                itemName = request.itemName,
                itemRarity = NotificationItemRarity.valueOf(request.itemRarity),
                itemType = NotificationItemType.valueOf(request.itemType),
            ),
        )
    }
}

data class InternalItemDropNotificationRequest(
    val sourceId: UUID,
    val occurredAt: Instant,
    val droppedFor: String,
    val itemKey: String?,
    val itemName: String,
    val itemRarity: String,
    val itemType: String,
)
