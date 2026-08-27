package com.mnemos.moderation.application.leaderboard.usecase.delete

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.leaderboard.mapper.LeaderboardExclusionIntegrationEventMapper.toIntegrationEvent
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionId
import com.mnemos.moderation.domain.leaderboard.repository.LeaderboardExclusionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional
class DeleteLeaderboardExclusionService(
    private val repository: LeaderboardExclusionRepository,
    private val currentModeratorProvider: CurrentModeratorProvider,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : DeleteLeaderboardExclusionUseCase {
    override fun execute(id: LeaderboardExclusionId) {
        val exclusion = repository.findById(id) ?: return
        val actor = currentModeratorProvider.current()
        val now = clock.instant()

        repository.delete(exclusion)

        publisher.publish(
            event = exclusion.toIntegrationEvent(active = false, occurredAt = now),
            metadata = EventMetadata(actorUserId = actor.userId.id),
        )
    }
}
