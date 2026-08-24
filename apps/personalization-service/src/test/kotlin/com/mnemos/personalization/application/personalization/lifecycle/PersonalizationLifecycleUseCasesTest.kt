package com.mnemos.personalization.application.personalization.lifecycle

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.asset.support.PersonalizationContentAssetValidator
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.event.port.out.NotificationPackSelectionRevisionStore
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.support.PersonalizationCreatorSupport
import com.mnemos.personalization.application.personalization.usecase.submit.SubmitPersonalizationService
import com.mnemos.personalization.application.personalization.usecase.updatecreator.UpdateCreatorPersonalizationService
import com.mnemos.personalization.application.personalization.usecase.updatecreator.UpdatePersonalizationCommand
import com.mnemos.personalization.application.personalization.usecase.updatecustompack.UpdateCustomNotificationPackCommand
import com.mnemos.personalization.application.personalization.usecase.updatecustompack.UpdateCustomNotificationPackService
import com.mnemos.personalization.application.pricing.PersonalizationPricePolicy
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.configuration.pricing.PersonalizationPricingProperties
import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import com.mnemos.personalization.domain.asset.model.PersonalizationAsset
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.asset.repository.PersonalizationAssetRepository
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PersonalizationLifecycleUseCasesTest {
    private val now = Instant.parse("2026-08-15T14:40:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `editing active custom notification pack republishes current snapshot`() {
        val userId = UserId.generate()
        val personalization = customNotificationPack(userId)
        val ownership = ownership(personalization, userId, active = true)
        val fixture = fixture(personalization, ownership, userId)
        val updatedContent = notificationPack("Updated")

        val result =
            fixture.updateCustomService.execute(
                UpdateCustomNotificationPackCommand(
                    personalizationId = personalization.id,
                    name = "Updated Pack",
                    content = updatedContent,
                ),
            )

        val event = fixture.publisher.events.single()
        val payload = event.payload as NotificationPackSelectionChangedV1

        assertEquals("Updated Pack", result.name)
        assertEquals(updatedContent, result.content)
        assertEquals(userId.id, event.aggregateId)
        assertEquals(personalization.id.id, payload.personalizationId)
        assertEquals(updatedContent.mixedAggregateTitle, payload.pack?.mixedAggregateTitle)
        assertEquals(1L, payload.selectionRevision)
    }

    @Test
    fun `editing inactive custom notification pack does not republish selection`() {
        val userId = UserId.generate()
        val personalization = customNotificationPack(userId)
        val ownership = ownership(personalization, userId, active = false)
        val fixture = fixture(personalization, ownership, userId)

        fixture.updateCustomService.execute(
            UpdateCustomNotificationPackCommand(
                personalizationId = personalization.id,
                name = "Updated Pack",
                content = notificationPack("Updated"),
            ),
        )

        assertTrue(fixture.publisher.events.isEmpty())
    }

    @Test
    fun `creator draft cannot be updated after creator role is removed`() {
        val userId = UserId.generate()
        val personalization = creatorNotificationPack(userId)
        val ownership = ownership(personalization, userId, active = false)
        val fixture = fixture(personalization, ownership, userId)

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            fixture.updateCreatorService.execute(
                UpdatePersonalizationCommand(
                    personalizationId = personalization.id,
                    name = "Updated creator pack",
                    content = notificationPack("Updated"),
                    price = personalization.price,
                ),
            )
        }
    }

    @Test
    fun `custom notification pack cannot be repriced through creator update flow`() {
        val userId = UserId.generate()
        val personalization = paidCustomNotificationPack(userId)
        val ownership = ownership(personalization, userId, active = false)
        val fixture =
            fixture(
                personalization = personalization,
                ownership = ownership,
                userId = userId,
                roles = setOf(UserRole.PERSONALIZATION_CREATOR),
            )

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            fixture.updateCreatorService.execute(
                UpdatePersonalizationCommand(
                    personalizationId = personalization.id,
                    name = "Cheaper custom pack",
                    content = notificationPack("Updated"),
                    price =
                        PersonalizationPrice(
                            currency = Currency.PRIME,
                            amount = 30,
                        ),
                ),
            )
        }

        assertEquals(50L, personalization.price?.amount)
        assertEquals(PersonalizationStatus.PRIVATE, personalization.status)
    }

    @Test
    fun `creator draft cannot be submitted after creator role is removed`() {
        val userId = UserId.generate()
        val personalization = creatorNotificationPack(userId)
        val ownership = ownership(personalization, userId, active = false)
        val fixture = fixture(personalization, ownership, userId)

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            fixture.submitService.execute(personalization.id)
        }
    }

    @Test
    fun `creator can submit owned custom notification pack`() {
        val userId = UserId.generate()
        val personalization = customNotificationPack(userId)
        val ownership = ownership(personalization, userId, active = false)
        val fixture =
            fixture(
                personalization = personalization,
                ownership = ownership,
                userId = userId,
                roles = setOf(UserRole.PERSONALIZATION_CREATOR),
            )

        val result = fixture.submitService.execute(personalization.id)

        assertEquals(PersonalizationStatus.IN_REVIEW, result.status)
    }

    @Test
    fun `creator cannot submit unpaid custom notification pack`() {
        val userId = UserId.generate()
        val personalization = paidCustomNotificationPack(userId)
        val unrelatedOwnership =
            OwnedPersonalization(
                owner = PersonalizationOwner.User(userId),
                personalizationId = PersonalizationId.generate(),
                type = PersonalizationType.NOTIFICATION_PACK,
                acquiredAt = now.minusSeconds(30),
                active = false,
            )
        val fixture =
            fixture(
                personalization = personalization,
                ownership = unrelatedOwnership,
                userId = userId,
                roles = setOf(UserRole.PERSONALIZATION_CREATOR),
            )

        assertThrows(PersonalizationNotOwnedException::class.java) {
            fixture.submitService.execute(personalization.id)
        }

        assertEquals(PersonalizationStatus.PRIVATE, personalization.status)
    }

    private fun fixture(
        personalization: Personalization,
        ownership: OwnedPersonalization,
        userId: UserId,
        roles: Set<UserRole> = emptySet(),
    ): Fixture {
        val personalizationRepository = FakePersonalizationRepository(personalization)
        val ownershipRepository = FakeOwnedPersonalizationRepository(ownership)
        val publisher = RecordingIntegrationEventPublisher()
        val selectionEventPublisher =
            NotificationPackSelectionEventPublisher(
                publisher = publisher,
                revisionStore = IncrementingSelectionRevisionStore(),
                clock = clock,
            )
        val support =
            PersonalizationCreatorSupport(
                actorProvider = FixedActorProvider(userId, roles),
                repository = personalizationRepository,
                ownershipRepository = ownershipRepository,
                selectionEventPublisher = selectionEventPublisher,
            )
        val assetValidator = PersonalizationContentAssetValidator(EmptyPersonalizationAssetRepository())

        return Fixture(
            updateCustomService =
                UpdateCustomNotificationPackService(
                    support = support,
                    repository = personalizationRepository,
                    clock = clock,
                ),
            updateCreatorService =
                UpdateCreatorPersonalizationService(
                    support = support,
                    pricePolicy = PersonalizationPricePolicy(PersonalizationPricingProperties()),
                    assetValidator = assetValidator,
                    repository = personalizationRepository,
                    clock = clock,
                ),
            submitService =
                SubmitPersonalizationService(
                    support = support,
                    repository = personalizationRepository,
                    clock = clock,
                ),
            publisher = publisher,
        )
    }

    private fun customNotificationPack(userId: UserId): Personalization =
        Personalization(
            id = PersonalizationId.generate(),
            creatorUserId = userId,
            type = PersonalizationType.NOTIFICATION_PACK,
            origin = PersonalizationOrigin.CUSTOM,
            visibility = PersonalizationVisibility.PRIVATE,
            name = "Custom Pack",
            content = notificationPack("Original"),
            price = null,
            status = PersonalizationStatus.PRIVATE,
            rejectionReason = null,
            createdAt = now.minusSeconds(60),
            updatedAt = now.minusSeconds(60),
        )

    private fun paidCustomNotificationPack(userId: UserId): Personalization =
        Personalization.createCustomNotificationPack(
            creatorUserId = userId,
            name = "Paid Custom Pack",
            content = notificationPack("Original"),
            price =
                PersonalizationPrice(
                    currency = Currency.PRIME,
                    amount = 50,
                ),
            now = now.minusSeconds(60),
        )

    private fun creatorNotificationPack(userId: UserId): Personalization =
        Personalization.createCreatorDraft(
            creatorUserId = userId,
            type = PersonalizationType.NOTIFICATION_PACK,
            name = "Creator Pack",
            content = notificationPack("Creator"),
            price =
                PersonalizationPrice(
                    currency = Currency.PRIME,
                    amount = 100,
                ),
            now = now.minusSeconds(60),
        )

    private fun ownership(
        personalization: Personalization,
        userId: UserId,
        active: Boolean,
    ): OwnedPersonalization =
        OwnedPersonalization(
            owner = PersonalizationOwner.User(userId),
            personalizationId = personalization.id,
            type = personalization.type,
            acquiredAt = now.minusSeconds(30),
            active = active,
        )

    private fun notificationPack(suffix: String): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = "Mixed $suffix",
            templates =
                NotificationTemplateKey.entries.associateWith { key ->
                    NotificationTemplate(
                        emoji = null,
                        soloTitle = "${key.name} title $suffix",
                        soloBody = "${key.name} body $suffix",
                        compact = "${key.name} compact $suffix",
                        aggregateTitle = "${key.name} aggregate $suffix",
                    )
                },
        )

    private data class Fixture(
        val updateCustomService: UpdateCustomNotificationPackService,
        val updateCreatorService: UpdateCreatorPersonalizationService,
        val submitService: SubmitPersonalizationService,
        val publisher: RecordingIntegrationEventPublisher,
    )

    private class FixedActorProvider(
        userId: UserId,
        roles: Set<UserRole>,
    ) : CurrentPersonalizationActorProvider {
        private val actor =
            PersonalizationActor(
                userId = userId,
                roles = roles,
            )

        override fun requireActor(): PersonalizationActor = actor
    }

    private class IncrementingSelectionRevisionStore : NotificationPackSelectionRevisionStore {
        private var revision = 0L

        override fun next(userId: UserId): Long {
            revision += 1

            return revision
        }

        override fun current(userId: UserId): Long = revision
    }

    private class EmptyPersonalizationAssetRepository : PersonalizationAssetRepository {
        override fun save(asset: PersonalizationAsset): PersonalizationAsset = asset

        override fun findById(id: PersonalizationAssetId): PersonalizationAsset? = null

        override fun findByOwnerUserId(ownerUserId: UserId): List<PersonalizationAsset> = emptyList()

        override fun existsByIdAndOwnerUserId(
            id: PersonalizationAssetId,
            ownerUserId: UserId,
        ): Boolean = false
    }

    private class FakePersonalizationRepository(
        private var personalization: Personalization,
    ) : PersonalizationRepository {
        override fun save(personalization: Personalization): Personalization {
            this.personalization = personalization

            return personalization
        }

        override fun findById(id: PersonalizationId): Personalization? = personalization.takeIf { it.id == id }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> =
            listOf(personalization).filter { it.creatorUserId == creatorUserId }

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> =
            listOf(personalization).filter { it.status == status }

        override fun isAssetReferencedByPublicPersonalization(assetId: PersonalizationAssetId): Boolean = false

        override fun isAssetReferencedByReviewablePersonalization(assetId: PersonalizationAssetId): Boolean = false
    }

    private class FakeOwnedPersonalizationRepository(
        private val ownership: OwnedPersonalization,
    ) : OwnedPersonalizationRepository {
        override fun save(ownership: OwnedPersonalization): OwnedPersonalization = ownership

        override fun exists(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean = ownership.owner == owner && ownership.personalizationId == personalizationId

        override fun find(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): OwnedPersonalization? =
            ownership.takeIf {
                it.owner == owner && it.personalizationId == personalizationId
            }

        override fun findActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ): OwnedPersonalization? =
            ownership.takeIf {
                it.owner == owner && it.type == type && it.active
            }

        override fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization> = listOf(ownership).filter { it.owner == owner }

        override fun findActiveAccountIds(types: Set<PersonalizationType>): List<AccountId> {
            val accountOwner = ownership.owner as? PersonalizationOwner.Account ?: return emptyList()

            if (!ownership.active || ownership.type !in types) {
                return emptyList()
            }

            return listOf(accountOwner.accountId)
        }

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) {
            if (ownership.owner == owner && ownership.type == type) {
                ownership.deactivate()
            }
        }
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val events = mutableListOf<IntegrationEvent<*>>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            events += event

            return MESSAGE_ID
        }
    }

    private companion object {
        val MESSAGE_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000031")
    }
}
