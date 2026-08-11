package com.mnemos.notification.adapter.inbound.scheduling.dungeon

import com.mnemos.notification.application.dungeon.usecase.process.ProcessDungeonEventCommand
import com.mnemos.notification.application.dungeon.usecase.process.ProcessDungeonEventUseCase
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class DungeonNotificationScheduler(
    private val processDungeonEventUseCase: ProcessDungeonEventUseCase,
) {
    @Scheduled(cron = "0 0,30 * * * *", zone = DUNGEON_TIME_ZONE)
    fun easy() {
        process(DomainDungeonType.EASY)
    }

    @Scheduled(cron = "0 15,45 * * * *", zone = DUNGEON_TIME_ZONE)
    fun medium() {
        process(DomainDungeonType.MEDIUM)
    }

    @Scheduled(cron = "0 10,40 * * * *", zone = DUNGEON_TIME_ZONE)
    fun hard() {
        process(DomainDungeonType.HARD)
    }

    @Scheduled(cron = "0 20,50 * * * *", zone = DUNGEON_TIME_ZONE)
    fun expert() {
        process(DomainDungeonType.EXPERT)
    }

    private fun process(dungeonType: DomainDungeonType) {
        processDungeonEventUseCase.execute(
            ProcessDungeonEventCommand(
                dungeonType = dungeonType,
                occurredAt = Instant.now().truncatedTo(ChronoUnit.MINUTES),
            ),
        )
    }

    private companion object {
        const val DUNGEON_TIME_ZONE = "\${mnemos.notification.dungeon.time-zone}"
    }
}
