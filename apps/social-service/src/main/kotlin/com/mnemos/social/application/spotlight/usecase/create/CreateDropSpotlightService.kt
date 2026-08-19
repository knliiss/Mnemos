package com.mnemos.social.application.spotlight.usecase.create

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.spotlight.exception.DropSpotlightIdempotencyConflictException
import com.mnemos.social.application.spotlight.exception.ItemDropAlreadySpotlightedException
import com.mnemos.social.application.spotlight.exception.SpotlightBuyerCooldownException
import com.mnemos.social.application.spotlight.exception.SpotlightItemDropAccessDeniedException
import com.mnemos.social.application.spotlight.exception.SpotlightItemDropNotFoundException
import com.mnemos.social.application.spotlight.exception.SpotlightPurchaseWindowClosedException
import com.mnemos.social.application.spotlight.exception.SpotlightQueueLimitException
import com.mnemos.social.application.spotlight.port.out.DropSpotlightQueue
import com.mnemos.social.application.spotlight.port.out.DropSpotlightWallet
import com.mnemos.social.application.spotlight.port.out.ItemDropLookup
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration

@Service
class CreateDropSpotlightService(
    private val actorProvider: SocialActorProvider,
    private val itemDropLookup: ItemDropLookup,
    private val repository: DropSpotlightRepository,
    private val queue: DropSpotlightQueue,
    private val wallet: DropSpotlightWallet,
    private val properties: DropSpotlightProperties,
    private val clock: Clock,
) : CreateDropSpotlightUseCase {
    @Transactional
    override fun execute(command: CreateDropSpotlightCommand): DropSpotlight {
        val actor = actorProvider.requireActor()
        val replay = command.idempotencyKey?.let(repository::findByIdempotencyKey)

        if (replay != null) {
            if (
                replay.buyerUserId != actor.userId ||
                replay.sourceAccountId != actor.accountId ||
                replay.itemDrop.id != command.itemDropEventId
            ) {
                throw DropSpotlightIdempotencyConflictException(requireNotNull(command.idempotencyKey))
            }

            return replay
        }

        val now = clock.instant()
        val itemDrop = itemDropLookup.findById(command.itemDropEventId)
            ?: throw SpotlightItemDropNotFoundException(command.itemDropEventId)

        if (!itemDrop.droppedFor.equals(actor.cristalixNickname, ignoreCase = true)) {
            throw SpotlightItemDropAccessDeniedException(command.itemDropEventId)
        }

        val age = Duration.between(itemDrop.occurredAt, now)

        if (age.isNegative || age > properties.dropMaxAge) {
            throw SpotlightPurchaseWindowClosedException(command.itemDropEventId)
        }

        val reservation = queue.reserve(now)

        if (repository.existsNonFailedByItemDropEventId(command.itemDropEventId)) {
            throw ItemDropAlreadySpotlightedException(command.itemDropEventId)
        }

        val latest = repository.findLatestByBuyerUserId(actor.userId)

        if (
            latest != null &&
            latest.status != DropSpotlightStatus.PAYMENT_FAILED &&
            latest.createdAt.plus(properties.buyerCooldown).isAfter(now)
        ) {
            throw SpotlightBuyerCooldownException()
        }

        if (reservation.scheduledAt.isAfter(now.plus(properties.maxQueueDelay))) {
            throw SpotlightQueueLimitException()
        }

        val spotlight =
            DropSpotlight.create(
                sequence = reservation.sequence,
                buyerUserId = actor.userId,
                sourceAccountId = actor.accountId,
                itemDrop = itemDrop,
                pricePrime = properties.pricePrime,
                scheduledAt = reservation.scheduledAt,
                now = now,
                idempotencyKey = command.idempotencyKey,
            )
        val saved = repository.save(spotlight)

        wallet.requestDebit(saved)

        return saved
    }
}
