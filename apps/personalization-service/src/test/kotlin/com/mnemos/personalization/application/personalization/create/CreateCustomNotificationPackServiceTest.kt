package com.mnemos.personalization.application.personalization.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.configuration.pricing.PersonalizationPricingProperties
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.CustomNotificationPackFreeGrantRepository
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CreateCustomNotificationPackServiceTest {
    private val now = Instant.parse("2026-08-15T15:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `creator receives free custom pack when atomic grant succeeds`() {
        val userId = UserId.generate()
        val grantRepository = FakeFreeGrantRepository(available = true)
        val ownershipRepository = FakeOwnedPersonalizationRepository()
        val fixture = fixture(userId, grantRepository, ownershipRepository)

        val result = fixture.execute()

        assertTrue(result.grantedForFree)
        assertNull(result.personalization.price)
        assertEquals(userId, grantRepository.claimedUserId)
        assertEquals(now, grantRepository.claimedAt)
        assertEquals(result.personalization.id, ownershipRepository.saved.single().personalizationId)
    }

    @Test
    fun `creator receives paid custom pack when free grant was already claimed`() {
        val userId = UserId.generate()
        val grantRepository = FakeFreeGrantRepository(available = false)
        val ownershipRepository = FakeOwnedPersonalizationRepository()
        val pricingProperties = PersonalizationPricingProperties()
        val fixture = fixture(userId, grantRepository, ownershipRepository, pricingProperties)

        val result = fixture.execute()

        assertFalse(result.grantedForFree)
        assertEquals(Currency.PRIME, result.personalization.price?.currency)
        assertEquals(pricingProperties.customNotificationPackPurchasePrime, result.personalization.price?.amount)
        assertTrue(ownershipRepository.saved.isEmpty())
    }

    private fun fixture(
        userId: UserId,
        grantRepository: FakeFreeGrantRepository,
        ownershipRepository: FakeOwnedPersonalizationRepository,
        pricingProperties: PersonalizationPricingProperties = PersonalizationPricingProperties(),
    ): Fixture {
        val service =
            CreateCustomNotificationPackService(
                actorProvider = FixedActorProvider(userId),
                personalizationRepository = FakePersonalizationRepository(),
                ownershipRepository = ownershipRepository,
                freeGrantRepository = grantRepository,
                pricePolicy = PersonalizationPricePolicy(pricingProperties),
                clock = clock,
            )

        return Fixture(service)
    }

    private data class Fixture(
        val service: CreateCustomNotificationPackService,
    ) {
        fun execute(): CreateCustomNotificationPackResult =
            service.execute(
                CreateCustomNotificationPackCommand(
                    name = "Custom Pack",
                    content = notificationPack(),
                ),
            )
    }

    private class FixedActorProvider(
        userId: UserId,
    ) : CurrentPersonalizationActorProvider {
        private val actor =
            PersonalizationActor(
                userId = userId,
                roles = setOf(UserRole.PERSONALIZATION_CREATOR),
            )

        override fun requireActor(): PersonalizationActor = actor
    }

    private class FakeFreeGrantRepository(
        private val available: Boolean,
    ) : CustomNotificationPackFreeGrantRepository {
        var claimedUserId: UserId? = null
        var claimedAt: Instant? = null

        override fun claim(
            userId: UserId,
            claimedAt: Instant,
        ): Boolean {
            claimedUserId = userId
            this.claimedAt = claimedAt

            return available
        }
    }

    private class FakePersonalizationRepository : PersonalizationRepository {
        private val personalizations = mutableListOf<Personalization>()

        override fun save(personalization: Personalization): Personalization {
            personalizations += personalization

            return personalization
        }

        override fun findById(id: PersonalizationId): Personalization? =
            personalizations.find { it.id == id }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> =
            personalizations.filter { it.creatorUserId == creatorUserId }

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> =
            personalizations.filter { it.status == status }

        override fun findPublished(
            type: PersonalizationType?,
            currency: Currency?,
            page: PageQuery,
        ): PageResult<Personalization> =
            PageResult(
                items = emptyList(),
                page = page.page,
                size = page.size,
                totalItems = 0,
                totalPages = 0,
                hasNextPage = false,
            )
    }

    private class FakeOwnedPersonalizationRepository : OwnedPersonalizationRepository {
        val saved = mutableListOf<OwnedPersonalization>()

        override fun save(ownership: OwnedPersonalization): OwnedPersonalization {
            saved += ownership

            return ownership
        }

        override fun exists(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean = false

        override fun find(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): OwnedPersonalization? = null

        override fun findActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ): OwnedPersonalization? = null

        override fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization> = emptyList()

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) = Unit
    }

    companion object {
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
}
