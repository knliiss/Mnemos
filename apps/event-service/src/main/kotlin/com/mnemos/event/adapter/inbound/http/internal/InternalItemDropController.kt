package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropQuery
import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropUseCase
import com.mnemos.event.domain.item.model.DomainItemType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/internal/v1/item-drops")
class InternalItemDropController(
    private val findFirstItemDropUseCase: FindFirstItemDropUseCase,
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
}
