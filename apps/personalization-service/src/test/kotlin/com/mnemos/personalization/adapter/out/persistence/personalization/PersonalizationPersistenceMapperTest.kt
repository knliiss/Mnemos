package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toEntity
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class PersonalizationPersistenceMapperTest {
    private val now = Instant.parse("2026-08-15T18:00:00Z")

    @Test
    fun `priced personalization survives entity round trip`() {
        val personalization =
            Personalization.createCustomNotificationPack(
                creatorUserId = UserId.generate(),
                name = "Paid custom pack",
                content = notificationPack(),
                price =
                    PersonalizationPrice(
                        currency = Currency.PRIME,
                        amount = 50,
                    ),
                now = now,
            )

        val restored = personalization.toEntity().toDomain()

        assertEquals(personalization.id, restored.id)
        assertEquals(personalization.creatorUserId, restored.creatorUserId)
        assertEquals(personalization.content, restored.content)
        assertEquals(personalization.price, restored.price)
        assertEquals(personalization.status, restored.status)
    }

    @Test
    fun `persisted price amount without currency is rejected`() {
        val entity = freeCustomNotificationPack().toEntity()
        entity.priceAmount = 50

        assertThrows(IllegalStateException::class.java) {
            entity.toDomain()
        }
    }

    @Test
    fun `persisted price currency without amount is rejected`() {
        val entity = freeCustomNotificationPack().toEntity()
        entity.priceCurrency = Currency.PRIME

        assertThrows(IllegalStateException::class.java) {
            entity.toDomain()
        }
    }

    private fun freeCustomNotificationPack(): Personalization =
        Personalization.createCustomNotificationPack(
            creatorUserId = UserId.generate(),
            name = "Free custom pack",
            content = notificationPack(),
            price = null,
            now = now,
        )

    private fun notificationPack(): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = "Mixed events",
            templates =
                NotificationTemplateKey.entries.associateWith { key ->
                    NotificationTemplate(
                        emoji = null,
                        soloTitle = "${key.name} title",
                        soloBody = "${key.name} body",
                        compact = "${key.name} compact",
                        aggregateTitle = "${key.name} aggregate",
                    )
                },
        )
}
