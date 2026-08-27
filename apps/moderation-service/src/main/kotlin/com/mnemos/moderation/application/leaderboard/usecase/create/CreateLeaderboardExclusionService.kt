package com.mnemos.moderation.application.leaderboard.usecase.create

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.leaderboard.mapper.LeaderboardExclusionIntegrationEventMapper.toIntegrationEvent
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion
import com.mnemos.moderation.domain.leaderboard.repository.LeaderboardExclusionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional
class CreateLeaderboardExclusionService(
    private val repository: LeaderboardExclusionRepository,
    private val currentModeratorProvider: CurrentModeratorProvider,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : CreateLeaderboardExclusionUseCase {
    override fun execute(command: CreateLeaderboardExclusionCommand): LeaderboardExclusion {
        repository
            .findBySubjectAndScope(
                subjectType = command.subjectType,
                subjectId = command.subjectId,
                scope = command.scope,
            )?.let { return it }

        val actor = currentModeratorProvider.current()
        val now = clock.instant()
        val exclusion =
            LeaderboardExclusion.create(
                subjectType = command.subjectType,
                subjectId = command.subjectId,
                scope = command.scope,
                reason = command.reason,
                createdByUserId = actor.userId,
                createdAt = now,
            )
        val saved = repository.save(exclusion)

        publisher.publish(
            event = saved.toIntegrationEvent(active = true, occurredAt = now),
            metadata = EventMetadata(actorUserId = actor.userId.id),
        )

        return saved
    }
}
