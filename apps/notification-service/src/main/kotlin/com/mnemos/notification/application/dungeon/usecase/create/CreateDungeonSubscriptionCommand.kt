package com.mnemos.notification.application.dungeon.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType

data class CreateDungeonSubscriptionCommand(
    val userId: UserId,
    val dungeonType: DomainDungeonType,
)
