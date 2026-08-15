package com.mnemos.social.application.spotlight

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.spotlight.port.out.DropSpotlightQueue
import com.mnemos.social.application.spotlight.port.out.DropSpotlightWallet
import com.mnemos.social.application.spotlight.port.out.ItemDropLookup
import com.mnemos.social.application.spotlight.port.out.NotificationPackSnapshotProvider
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Duration
import java.util.UUID

@Service
class CreateDropSpotlightService(
    private val actorProvider: SocialActorProvider,
    private val itemDropLookup: ItemDropLookup,
    private val notificationPackSnapshotProvider: NotificationPackSnapshotProvider,
    private val repository: DropSpotlightRepository,
    private val queue: DropSpotlightQueue,
    private val wallet: DropSpotlightWallet,
    private val properties: DropSpotlightProperties,
    private val clock: Clock,
) {
    @Transactional
    fun execute(itemDropEventId: UUID): DropSpotlight {
        val actor = actorProvider.requireActor()
        val now = clock.instant()
        val itemDrop =
            itemDropLookup.findById(itemDropEventId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Item drop was not found")

        if (!itemDrop.droppedFor.equals(actor.cristalixNickname, ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Item drop does not belong to the active account")
        }

        val age = Duration.between(itemDrop.occurredAt, now)

        if (age.isNegative || age > properties.dropMaxAge) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Item drop is outside the spotlight purchase window")
        }

        val reservation = queue.reserve(now)

        if (repository.existsByItemDropEventId(itemDropEventId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Item drop has already been spotlighted")
        }

        val latest = repository.findLatestByBuyerUserId(actor.userId)

        if (
            latest != null &&
            latest.status != DropSpotlightStatus.PAYMENT_FAILED &&
            latest.createdAt.plus(properties.buyerCooldown).isAfter(now)
        ) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Spotlight buyer cooldown is active")
        }

        if (reservation.scheduledAt.isAfter(now.plus(properties.maxQueueDelay))) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Spotlight queue is longer than allowed")
        }

        val spotlight =
            DropSpotlight.create(
                sequence = reservation.sequence,
                buyerUserId = actor.userId,
                sourceAccountId = actor.accountId,
                itemDrop = itemDrop,
                notificationPack = notificationPackSnapshotProvider.findActive(actor.userId),
                pricePrime = properties.pricePrime,
                scheduledAt = reservation.scheduledAt,
                now = now,
            )
        val saved = repository.save(spotlight)

        wallet.requestDebit(saved)

        return saved
    }
}
