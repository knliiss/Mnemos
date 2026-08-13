package com.mnemos.moderation.application.sanction.usecase.expire

import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.moderation.application.sanction.mapper.SanctionIntegrationEventMapper.toDeactivatedIntegrationEvent
import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional
class ExpireSanctionsService(
    private val sanctionRepository: SanctionRepository,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : ExpireSanctionsUseCase {
    override fun execute() {
        val now = clock.instant()
        val expiredCandidates = sanctionRepository.findExpired(now, 100)

        expiredCandidates.forEach { candidate ->
            val sanction = sanctionRepository.findByIdForUpdate(candidate.id) ?: return@forEach

            if (!sanction.expire(now)) {
                return@forEach
            }

            val saved = sanctionRepository.save(sanction)
            publisher.publish(saved.toDeactivatedIntegrationEvent(now))
        }
    }
}
