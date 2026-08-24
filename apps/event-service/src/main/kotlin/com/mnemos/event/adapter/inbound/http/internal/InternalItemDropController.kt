package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalEventApiMapper.toFirstResponse
import com.mnemos.event.adapter.inbound.http.internal.InternalEventApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.internal.InternalItemDropApiMapper.toInternalResponse
import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropUseCase
import com.mnemos.event.application.itemdrop.usecase.get.GetItemDropUseCase
import com.mnemos.event.domain.EventId
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/item-drops")
@PreAuthorize(
    "authentication.name == 'bet-service' or " +
        "authentication.name == 'social-service'",
)
class InternalItemDropController(
    private val findFirstItemDropUseCase: FindFirstItemDropUseCase,
    private val getItemDropUseCase: GetItemDropUseCase,
) {
    @GetMapping("/first")
    fun first(
        @Valid @ModelAttribute request: FindFirstItemDropRequest,
    ): FirstItemDropResponse? =
        findFirstItemDropUseCase
            .execute(request.toQuery())
            ?.toFirstResponse()

    @GetMapping("/{itemDropId}")
    fun getById(
        @PathVariable
        itemDropId: UUID,
    ): ResponseEntity<InternalItemDropResponse> {
        val itemDrop =
            getItemDropUseCase.execute(EventId(itemDropId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(itemDrop.toInternalResponse())
    }
}
