package com.mnemos.social.application.spotlight

import com.mnemos.social.application.spotlight.port.out.DropSpotlightEventPublisher
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class PublishDueDropSpotlightsService(
    private val repository: DropSpotlightRepository,
    private val publisher: DropSpotlightEventPublisher,
    private val properties: DropSpotlightProperties,
    private val clock: Clock,
) {
    @Transactional
    fun execute() {
        val now = clock.instant()
        val due = repository.findDue(now)

        due.forEach { spotlight ->
            spotlight.publish(
                now = now,
                expiresAt = now.plus(properties.lifetime),
            )
            val saved = repository.save(spotlight)
            publisher.publish(saved)
        }
    }
}
