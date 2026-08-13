package com.mnemos.moderation.application.sanction.usecase.revoke

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.sanction.mapper.SanctionIntegrationEventMapper.toDeactivatedIntegrationEvent
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.application.security.port.out.UserRoleProvider
import com.mnemos.moderation.domain.sanction.exception.SanctionNotFoundException
import com.mnemos.moderation.domain.sanction.policy.SanctionPolicy
import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional
class RevokeSanctionService(
    private val sanctionRepository: SanctionRepository,
    private val currentModeratorProvider: CurrentModeratorProvider,
    private val userRoleProvider: UserRoleProvider,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : RevokeSanctionUseCase {
    override fun execute(command: RevokeSanctionCommand) {
        val sanction =
            sanctionRepository.findByIdForUpdate(command.sanctionId)
                ?: throw SanctionNotFoundException(command.sanctionId)

        val actor = currentModeratorProvider.current()
        val targetRole = userRoleProvider.highestRole(sanction.userId)

        SanctionPolicy.validateTarget(
            actorUserId = actor.userId,
            actorRole = actor.role,
            targetUserId = sanction.userId,
            targetRole = targetRole,
        )

        if (!sanction.revoke()) {
            return
        }

        val now = clock.instant()
        val saved = sanctionRepository.save(sanction)

        publisher.publish(
            event = saved.toDeactivatedIntegrationEvent(now),
            metadata = EventMetadata(actorUserId = actor.userId.id),
        )
    }
}
