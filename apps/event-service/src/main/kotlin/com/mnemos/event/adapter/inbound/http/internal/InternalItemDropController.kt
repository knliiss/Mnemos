package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropQuery
import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropUseCase
import com.mnemos.event.application.itemdrop.usecase.get.GetItemDropUseCase
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/item-drops")
class InternalItemDropController(
    private val findFirstItemDropUseCase: FindFirstItemDropUseCase,
    private val getItemDropUseCase: GetItemDropUseCase,
) {
    @GetMapping("/first")
    fun first(
        @RequestParam
        itemName: String,
        @RequestParam
        itemType: DomainItemType,
        @RequestParam
        from: Instant,
        @RequestParam
        to: Instant,
    ): FirstItemDropResponse? =
        findFirstItemDropUseCase
            .execute(
                FindFirstItemDropQuery(
                    itemName = itemName,
                    itemType = itemType,
                    from = from,
                    to = to,
                ),
            )?.let { itemDrop ->
                FirstItemDropResponse(
                    droppedFor = itemDrop.droppedFor,
                    occurredAt = itemDrop.occurredAt,
                )
            }

    @GetMapping("/{itemDropId}")
    fun getById(@PathVariable itemDropId: UUID): ResponseEntity<InternalItemDropResponse> {
        val itemDrop = getItemDropUseCase.execute(EventId(itemDropId))
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            InternalItemDropResponse(
                id = itemDrop.id.id,
                itemId = itemDrop.itemId.id,
                itemName = itemDrop.itemName,
                itemRarity = itemDrop.itemRarity,
                itemType = itemDrop.itemType,
                droppedFor = itemDrop.droppedFor,
                occurredAt = itemDrop.occurredAt,
            ),
        )
    }
}

data class InternalItemDropResponse(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: DomainItemType,
    val droppedFor: String,
    val occurredAt: Instant,
)
