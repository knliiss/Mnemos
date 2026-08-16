package com.mnemos.social.application.spotlight.usecase.processwallet

import com.mnemos.social.application.spotlight.model.SpotlightWalletResult
import com.mnemos.social.domain.spotlight.model.DropSpotlight
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
) : ProcessDropSpotlightWalletResultUseCase {
    @Transactional
    override fun completed(
        result: SpotlightWalletResult,
        transactionId: UUID,
    ) {
        val spotlight = repository.findByIdForUpdate(DropSpotlightId(result.spotlightId)) ?: return

        requireMatchingWalletResult(spotlight, result)

        val changed =
            spotlight.paymentCompleted(
                transactionId = transactionId,
                now = clock.instant(),
            )

        if (changed) {
            repository.save(spotlight)
        }
    }

    @Transactional
    override fun failed(result: SpotlightWalletResult) {
        val spotlight = repository.findByIdForUpdate(DropSpotlightId(result.spotlightId)) ?: return

        requireMatchingWalletResult(spotlight, result)

        if (spotlight.paymentFailed(clock.instant())) {
            repository.save(spotlight)
        }
    }

    private fun requireMatchingWalletResult(
        spotlight: DropSpotlight,
        result: SpotlightWalletResult,
    ) {
        check(spotlight.walletRequestId == result.requestId) {
            "Spotlight wallet result requestId does not match the pending request"
        }
        check(spotlight.buyerUserId == result.ownerUserId) {
            "Spotlight wallet result owner does not match the buyer user"
        }
        check(spotlight.pricePrime == result.amount) {
            "Spotlight wallet result amount does not match the purchase price"
        }
    }
}
