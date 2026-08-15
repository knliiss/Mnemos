package com.mnemos.bet.application.itemrace.usecase.get

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.common.core.model.permission.RolePermission.VIEW_BETS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetItemRaceService(
    private val reader: ItemRaceReader,
    private val accountProvider: AccountProvider,
) : GetItemRaceUseCase {
    override fun execute(query: GetItemRaceQuery): GetItemRaceResult {
        accountProvider.requireActiveAccount(VIEW_BETS)

        val race =
            reader.find(query.raceId)
                ?: throw BetNotFoundException("Item race ${query.raceId} was not found")

        return GetItemRaceResult(
            race = race,
            participants = reader.findParticipants(race.id),
        )
    }
}
