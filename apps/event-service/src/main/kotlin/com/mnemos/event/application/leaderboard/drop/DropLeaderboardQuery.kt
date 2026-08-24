package com.mnemos.event.application.leaderboard.drop

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.wipe.model.WipeId

data class DropLeaderboardQuery(
    val wipeId: WipeId?,
    val rarity: DomainItemRarity?,
    val itemType: DomainItemType?,
    val itemName: String?,
    val nickname: String?,
    val sort: DropLeaderboardSort,
    val limit: Int,
)
