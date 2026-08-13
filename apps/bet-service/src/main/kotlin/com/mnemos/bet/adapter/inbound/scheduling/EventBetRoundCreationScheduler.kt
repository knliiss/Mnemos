package com.mnemos.bet.adapter.inbound.scheduling

import com.mnemos.bet.application.eventbet.service.EventBetSchedulePlanner
import com.mnemos.bet.application.eventbet.usecase.create.CreateEventBetRoundCommand
import com.mnemos.bet.application.eventbet.usecase.create.CreateEventBetRoundUseCase
import com.mnemos.bet.configuration.EventBetProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class EventBetRoundCreationScheduler(
    private val planner: EventBetSchedulePlanner,
    private val createEventBetRoundUseCase: CreateEventBetRoundUseCase,
    private val properties: EventBetProperties,
    private val clock: Clock,
) {
    @Scheduled(fixedDelayString = "\${mnemos.bet.scheduler.round-creation-delay:30s}")
    fun createUpcomingRounds() {
        val now = clock.instant()
        val to = now.plus(properties.scheduleHorizon)

        planner.slots(now, to).forEach { slot ->
            createEventBetRoundUseCase.execute(
                CreateEventBetRoundCommand(
                    eventType = slot.eventType,
                    scheduledAt = slot.scheduledAt,
                ),
            )
        }
    }
}
