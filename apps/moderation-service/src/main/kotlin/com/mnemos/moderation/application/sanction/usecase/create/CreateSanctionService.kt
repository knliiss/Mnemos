package com.mnemos.moderation.application.sanction.usecase.create

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.sanction.mapper.SanctionIntegrationEventMapper.toActivatedIntegrationEvent
import com.mnemos.moderation.application.sanction.mapper.SanctionIntegrationEventMapper.toDeactivatedIntegrationEvent
import com.mnemos.moderation.application.sanction.mapper.SanctionViewMapper.toView
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.application.security.port.out.UserRoleProvider
import com.mnemos.moderation.domain.sanction.exception.SanctionConflictException
import com.mnemos.moderation.domain.sanction.exception.SelfSanctionException
import com.mnemos.moderation.domain.sanction.model.Sanction
import com.mnemos.moderation.domain.sanction.policy.SanctionPolicy
import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional
class CreateSanctionService(
    private val sanctionRepository: SanctionRepository,
    private val currentModeratorProvider: CurrentModeratorProvider,
    private val userRoleProvider: UserRoleProvider,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : CreateSanctionUseCase {
    override fun execute(command: CreateSanctionCommand): CreateSanctionResult {
        val actor = currentModeratorProvider.current()

        if (actor.userId == command.userId) {
            throw SelfSanctionException()
        }

        val targetRole = userRoleProvider.highestRole(command.userId)
        val now = clock.instant()
        val expiresAt = command.duration?.let(now::plus)

        SanctionPolicy.validateCreation(
            actorUserId = actor.userId,
            actorRole = actor.role,
            targetUserId = command.userId,
            targetRole = targetRole,
            createdAt = now,
            expiresAt = expiresAt,
        )

        val existing =
            sanctionRepository.findActiveByUserIdAndTypeForUpdate(
                userId = command.userId,
                type = command.type,
            )

        if (existing != null) {
            if (!existing.expire(now)) {
                throw SanctionConflictException(
                    userId = command.userId,
                    type = command.type,
                )
            }

            val expired = sanctionRepository.save(existing)
            publisher.publish(expired.toDeactivatedIntegrationEvent(now))
        }

        val sanction =
            Sanction.create(
                userId = command.userId,
                type = command.type,
                reason = command.reason,
                internalNote = command.internalNote,
                createdByUserId = actor.userId,
                createdAt = now,
                expiresAt = expiresAt,
            )

        val saved = sanctionRepository.save(sanction)

        publisher.publish(
            event = saved.toActivatedIntegrationEvent(now),
            metadata = EventMetadata(actorUserId = actor.userId.id),
        )

        return CreateSanctionResult(
            sanction = saved.toView(),
        )
    }
}
