package com.mnemos.notification.adapter.inbound.http.dungeon.create

import com.mnemos.notification.domain.dungeon.model.DomainDungeonType

data class CreateDungeonSubscriptionRequest(
    val dungeonType: DomainDungeonType,
)
