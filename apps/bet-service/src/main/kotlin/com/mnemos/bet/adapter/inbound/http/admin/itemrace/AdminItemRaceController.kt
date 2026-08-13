package com.mnemos.bet.adapter.inbound.http.admin.itemrace

import com.mnemos.bet.application.itemrace.usecase.cancel.CancelItemRaceCommand
import com.mnemos.bet.application.itemrace.usecase.cancel.CancelItemRaceUseCase
import com.mnemos.bet.application.itemrace.usecase.create.CreateItemRaceCommand
import com.mnemos.bet.application.itemrace.usecase.create.CreateItemRaceUseCase
import com.mnemos.bet.domain.itemrace.model.DomainItemType
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/admin/item-races")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminItemRaceController(
    private val createItemRaceUseCase: CreateItemRaceUseCase,
    private val cancelItemRaceUseCase: CancelItemRaceUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody
        @Valid
        request: CreateItemRaceRequest,
    ): UUID =
        createItemRaceUseCase
            .execute(
                CreateItemRaceCommand(
                    itemName = request.itemName,
                    itemType = DomainItemType.valueOf(request.itemType.name),
                    durationHours = request.durationHours,
                    entryAmount = request.entryAmount,
                ),
            ).raceId
            .id

    @DeleteMapping("/{raceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @PathVariable
        raceId: UUID,
    ) {
        cancelItemRaceUseCase.execute(
            CancelItemRaceCommand(
                raceId = ItemRaceId(raceId),
            ),
        )
    }
}
