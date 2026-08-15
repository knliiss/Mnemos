package com.mnemos.personalization.domain.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class CustomNotificationPackTest {
    private val creatorUserId = UserId.generate()
    private val otherUserId = UserId.generate()
    private val createdAt = Instant.parse("2026-08-15T00:00:00Z")

    @Test
    fun `private custom pack is available only to its author`() {
        val personalization = createCustomPack()

        assertTrue(personalization.isPurchasableBy(creatorUserId))
        assertFalse(personalization.isPurchasableBy(otherUserId))
        assertTrue(personalization.status == PersonalizationStatus.PRIVATE)
    }

    @Test
    fun `custom pack can become public after moderation`() {
        val personalization = createCustomPack()

        personalization.submitForReview(createdAt.plusSeconds(1))
        personalization.approve(
            moderatedPrice = PersonalizationPrice(Currency.PRIME, 25),
            now = createdAt.plusSeconds(2),
        )
        personalization.publish(createdAt.plusSeconds(3))

        assertTrue(personalization.isPurchasableBy(otherUserId))
        assertTrue(personalization.visibility == PersonalizationVisibility.PUBLIC)
        assertTrue(personalization.status == PersonalizationStatus.PUBLISHED)
    }

    private fun createCustomPack(): Personalization =
        Personalization.createCustomNotificationPack(
            creatorUserId = creatorUserId,
            name = "Personal Ice Pack",
            content = notificationPackContent(),
            price = PersonalizationPrice(Currency.PRIME, 30),
            now = createdAt,
        )

    private fun notificationPackContent(): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = "❄️ <b>Several events</b>",
            templates =
                NotificationTemplateKey.entries.associateWith { key ->
                    NotificationTemplate(
                        emoji = "❄️",
                        soloTitle = key.name,
                        soloBody = "Notification",
                        compact = "Notification",
                        aggregateTitle = "${key.name} events",
                    )
                },
        )
}
