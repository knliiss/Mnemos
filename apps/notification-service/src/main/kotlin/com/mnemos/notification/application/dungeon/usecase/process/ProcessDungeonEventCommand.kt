package com.mnemos.notification.application.dungeon.usecase.process

import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import java.time.Instant

data class ProcessDungeonEventCommand(
    val occurredAt: Instant,
    val dungeonType: DomainDungeonType,
)
