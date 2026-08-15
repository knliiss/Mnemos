package com.mnemos.social.domain.spotlight.model

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.social.application.spotlight.model.ItemDropSnapshot
import com.mnemos.social.application.spotlight.model.NotificationPackSelectionSnapshot
import java.time.Instant
import java.util.UUID

data class DropSpotlight(
    val id: DropSpotlightId,
    val sequence: Long,
    val buyerUserId: UUID,
    val sourceAccountId: UUID,
    val itemDrop: ItemDropSnapshot,
    val notificationPack: NotificationPackSelectionSnapshot?,
    val pricePrime: Long,
    val walletRequestId: UUID,
    var walletTransactionId: UUID?,
    var status: DropSpotlightStatus,
    val scheduledAt: Instant,
    var publishedAt: Instant?,
    var expiresAt: Instant?,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    fun paymentCompleted(
        transactionId: UUID,
        now: Instant,
    ) {
        if (status != DropSpotlightStatus.PENDING_PAYMENT) {
            return
        }

        walletTransactionId = transactionId
        status = DropSpotlightStatus.QUEUED
        updatedAt = now
    }

    fun paymentFailed(now: Instant) {
        if (status != DropSpotlightStatus.PENDING_PAYMENT) {
            return
        }

        status = DropSpotlightStatus.PAYMENT_FAILED
        updatedAt = now
    }

    fun publish(
        now: Instant,
        expiresAt: Instant,
    ) {
        require(status == DropSpotlightStatus.QUEUED) { "Only queued spotlight can be published" }
        status = DropSpotlightStatus.PUBLISHED
        publishedAt = now
        this.expiresAt = expiresAt
        updatedAt = now
    }

    companion object {
        fun create(
            sequence: Long,
            buyerUserId: UUID,
            sourceAccountId: UUID,
            itemDrop: ItemDropSnapshot,
            notificationPack: NotificationPackSelectionSnapshot?,
            pricePrime: Long,
            scheduledAt: Instant,
            now: Instant,
        ): DropSpotlight =
            DropSpotlight(
                id = DropSpotlightId.generate(),
                sequence = sequence,
                buyerUserId = buyerUserId,
                sourceAccountId = sourceAccountId,
                itemDrop = itemDrop,
                notificationPack = notificationPack,
                pricePrime = pricePrime,
                walletRequestId = UuidCreator.getTimeOrderedEpoch(),
                walletTransactionId = null,
                status = DropSpotlightStatus.PENDING_PAYMENT,
                scheduledAt = scheduledAt,
                publishedAt = null,
                expiresAt = null,
                createdAt = now,
                updatedAt = now,
            )
    }
}
