package com.mnemos.moderation.application.sanction.usecase.active

import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
@Transactional(readOnly = true)
class GetActiveBansService(
    private val sanctionRepository: SanctionRepository,
    private val clock: Clock,
) : GetActiveBansUseCase {
    override fun execute(): List<ActiveBanView> =
        sanctionRepository
            .findActiveBans(clock.instant())
            .map { sanction ->
                ActiveBanView(
                    sanctionId = sanction.id,
                    userId = sanction.userId,
                    occurredAt = sanction.createdAt,
                )
            }
}
