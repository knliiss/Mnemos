package com.mnemos.event.adapter.inbound.http.leaderboard

import com.mnemos.event.application.leaderboard.drop.DropLeaderboardSort
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.util.UUID

data class DropLeaderboardRequest(
    val wipeId: UUID? = null,
    val rarity: DomainItemRarity? = null,
    val itemType: DomainItemType? = null,
    @field:Size(max = 24)
    val itemName: String? = null,
    @field:Size(max = 20)
    val nickname: String? = null,
    val sort: DropLeaderboardSort = DropLeaderboardSort.TOTAL,
    @field:Min(3)
    @field:Max(100)
    val limit: Int = 50,
)
