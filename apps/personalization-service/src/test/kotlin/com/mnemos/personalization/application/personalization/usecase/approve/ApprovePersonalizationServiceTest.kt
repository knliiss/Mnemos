package com.mnemos.personalization.application.personalization.usecase.approve

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.support.PersonalizationModerationSupport
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.configuration.pricing.PersonalizationPricingProperties
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ApprovePersonalizationServiceTest {
    private val now = Instant.parse("2026-08-15T16:30:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `personalization moderator cannot approve own personalization`() {
        val creatorUserId = UserId.generate()
        val personalization = personalizationInReview(creatorUserId)
        val service =
            service(
                personalization = personalization,
                actor =
                    PersonalizationActor(
                        userId = creatorUserId,
                        roles = setOf(UserRole.PERSONALIZATION_MODERATOR),
                    ),
            )

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            service.execute(
                personalizationId = personalization.id,
                price = PersonalizationPrice(Currency.COIN, 1_000),
            )
        }
    }

    @Test
    fun `personalization moderator approval publishes another creators personalization`() {
        val personalization = personalizationInReview(UserId.generate())
        val service =
            service(
                personalization = personalization,
                actor =
                    PersonalizationActor(
                        userId = UserId.generate(),
                        roles = setOf(UserRole.PERSONALIZATION_MODERATOR),
                    ),
            )

        val result =
            service.execute(
                personalizationId = personalization.id,
                price = PersonalizationPrice(Currency.COIN, 1_000),
            )

        assertEquals(PersonalizationStatus.PUBLISHED, result.status)
    }

    @Test
    fun `administrator approval publishes own personalization`() {
        val creatorUserId = UserId.generate()
        val personalization = personalizationInReview(creatorUserId)
        val service =
            service(
                personalization = personalization,
                actor =
                    PersonalizationActor(
                        userId = creatorUserId,
                        roles = setOf(UserRole.ADMIN),
                    ),
            )

        val result =
            service.execute(
                personalizationId = personalization.id,
                price = PersonalizationPrice(Currency.COIN, 1_000),
            )

        assertEquals(PersonalizationStatus.PUBLISHED, result.status)
    }

    private fun service(
        personalization: Personalization,
        actor: PersonalizationActor,
    ): ApprovePersonalizationService {
        val repository = FakePersonalizationRepository(personalization)
        val support =
            PersonalizationModerationSupport(
                actorProvider = FixedActorProvider(actor),
                repository = repository,
            )

        return ApprovePersonalizationService(
            support = support,
            pricePolicy = PersonalizationPricePolicy(PersonalizationPricingProperties()),
            repository = repository,
            clock = clock,
        )
    }

    private fun personalizationInReview(creatorUserId: UserId): Personalization =
        Personalization
            .createCreatorDraft(
                creatorUserId = creatorUserId,
                type = PersonalizationType.NICK_STYLE,
                name = "Ice Gradient",
                content = nickStyleContent(),
                price = PersonalizationPrice(Currency.COIN, 1_000),
                now = now.minusSeconds(60),
            ).also { personalization ->
                personalization.submitForReview(now.minusSeconds(30))
            }

    private fun nickStyleContent(): PersonalizationContent.NickStyle =
        PersonalizationContent.NickStyle(
            colors =
                listOf(
                    GradientStop(
                        color = HexColor("#F5FBFF"),
                        position = 0,
                    ),
                    GradientStop(
                        color = HexColor("#8ACBFF"),
                        position = 100,
                    ),
                ),
            angleDegrees = 90,
            glow = null,
        )

    private class FixedActorProvider(
        private val actor: PersonalizationActor,
    ) : CurrentPersonalizationActorProvider {
        override fun requireActor(): PersonalizationActor = actor
    }

    private class FakePersonalizationRepository(
        private var personalization: Personalization,
    ) : PersonalizationRepository {
        override fun save(personalization: Personalization): Personalization {
            this.personalization = personalization

            return personalization
        }

        override fun findById(id: PersonalizationId): Personalization? = personalization.takeIf { it.id == id }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> = emptyList()

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> =
            listOf(personalization).filter { it.status == status }

        override fun isAssetReferencedByPublicPersonalization(assetId: PersonalizationAssetId): Boolean = false

        override fun isAssetReferencedByReviewablePersonalization(assetId: PersonalizationAssetId): Boolean = false
    }
}
