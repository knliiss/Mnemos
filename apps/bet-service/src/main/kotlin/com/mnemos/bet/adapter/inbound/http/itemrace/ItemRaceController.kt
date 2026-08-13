package com.mnemos.bet.adapter.inbound.http.itemrace

import com.mnemos.bet.adapter.inbound.http.itemrace.ItemRaceApiMapper.toResponse
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceQuery
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceUseCase
import com.mnemos.bet.application.itemrace.usecase.list.ListItemRacesUseCase
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/item-races")
class ItemRaceController(
    private val listItemRacesUseCase: ListItemRacesUseCase,
    private val getItemRaceUseCase: GetItemRaceUseCase,
) {
    @GetMapping
    fun list(): List<ItemRaceResponse> = listItemRacesUseCase.execute().map { it.toResponse() }

    @GetMapping("/{raceId}")
    fun get(
        @PathVariable
        raceId: UUID,
    ): ItemRaceResponse =
        getItemRaceUseCase
            .execute(
                GetItemRaceQuery(
                    raceId = ItemRaceId(raceId),
                ),
            ).toResponse()
}
