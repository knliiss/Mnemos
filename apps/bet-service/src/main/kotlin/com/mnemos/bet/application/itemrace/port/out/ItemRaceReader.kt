package com.mnemos.bet.application.itemrace.port.out

import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.common.core.model.id.AccountId

interface ItemRaceReader {
    fun findCurrent(): List<ItemRaceView>

    fun find(raceId: ItemRaceId): ItemRaceView?

    fun findParticipants(raceId: ItemRaceId): List<ItemRaceParticipantView>

    fun findParticipant(
        raceId: ItemRaceId,
        accountId: AccountId,
    ): ItemRaceParticipantView?

    fun findHistory(accountId: AccountId): List<ItemRaceParticipantView>
}
