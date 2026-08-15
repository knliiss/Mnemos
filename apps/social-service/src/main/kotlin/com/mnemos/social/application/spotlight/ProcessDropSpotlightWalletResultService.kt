package com.mnemos.social.application.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class ProcessDropSpotlightWalletResultService(
    private val repository: DropSpotlightRepository,
    private val clock: Clock,
) {
    @Transactional
    fun completed(
        spotlightId: UUID,
        transactionId: UUID,
    ) {
        val spotlight = repository.findById(DropSpotlightId(spotlightId)) ?: return
        spotlight.paymentCompleted(transactionId, clock.instant())
        repository.save(spotlight)
    }

    @Transactional
    fun failed(spotlightId: UUID) {
        val spotlight = repository.findById(DropSpotlightId(spotlightId)) ?: return
        spotlight.paymentFailed(clock.instant())
        repository.save(spotlight)
    }
}
