package com.mnemos.social.application.spotlight.usecase.get

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GetDropSpotlightService(
    private val actorProvider: SocialActorProvider,
    private val repository: DropSpotlightRepository,
) {
    @Transactional(readOnly = true)
    fun execute(spotlightId: DropSpotlightId): DropSpotlight {
        val userId = actorProvider.requireUserId()
        val spotlight = repository.findById(spotlightId)

        if (spotlight == null || spotlight.buyerUserId != userId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Drop spotlight was not found")
        }

        return spotlight
    }
}
