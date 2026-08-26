package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.ownership.event.port.out.NotificationPackSelectionRevisionStore
import com.mnemos.personalization.application.ownership.event.port.out.ProfileAppearanceRevisionStore
import com.mnemos.personalization.application.personalization.builtin.model.BuiltinPersonalizationCatalogState
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationCatalogStateRepository
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationLookup
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationOwnershipReader
import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class SynchronizeBuiltinPersonalizationCatalogServiceTest {
    private val now = Instant.parse("2026-08-25T20:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `creates missing builtins and advances catalog revision`() {
        val fixture = fixture(initialRevision = 0)

        fixture.service.execute()

        val definitions = BuiltinPersonalizationCatalog.definitions
        val personalizations =
            fixture.personalizationStore.byCatalogKey
                .values
                .toList()

        assertEquals(41, definitions.size)
        assertEquals(9, definitions.count { it.type == PersonalizationType.NOTIFICATION_PACK })
        assertEquals(8, definitions.count { it.type == PersonalizationType.NICK_STYLE })
        assertEquals(8, definitions.count { it.type == PersonalizationType.PROFILE_THEME })
        assertEquals(8, definitions.count { it.type == PersonalizationType.AVATAR_FRAME })
        assertEquals(8, definitions.count { it.type == PersonalizationType.PROFILE_EFFECT })
        assertEquals(definitions.size, personalizations.size)
        assertEquals(definitions.size, fixture.personalizationStore.saveCalls)
        assertEquals(definitions.map { it.catalogKey }.toSet(), fixture.personalizationStore.byCatalogKey.keys)
        assertTrue(personalizations.all { it.origin == PersonalizationOrigin.SYSTEM })
        assertTrue(personalizations.all { it.status == PersonalizationStatus.PUBLISHED })
        assertTrue(personalizations.all { it.creatorUserId == null })
        assertEquals(4, fixture.stateRepository.state.revision)
        assertEquals(now, fixture.stateRepository.state.updatedAt)
        assertEquals(1, fixture.stateRepository.updateCalls)
        assertTrue(fixture.publisher.events.isEmpty())
    }

    @Test
    fun `repeated synchronization is idempotent`() {
        val fixture = fixture(initialRevision = 0)

        fixture.service.execute()
        fixture.service.execute()

        assertEquals(BuiltinPersonalizationCatalog.definitions.size, fixture.personalizationStore.saveCalls)
        assertEquals(BuiltinPersonalizationCatalog.definitions.size, fixture.personalizationStore.byCatalogKey.size)
        assertEquals(1, fixture.stateRepository.updateCalls)
        assertTrue(fixture.publisher.events.isEmpty())
    }

    @Test
    fun `updates existing system pack without replacing identity and republishes active snapshot`() {
        val definitions = BuiltinPersonalizationCatalog.definitions
        val firstDefinition = definitions.first()
        val existing =
            definitions.mapIndexed { index, definition ->
                Personalization.createSystem(
                    catalogKey = definition.catalogKey,
                    type = definition.type,
                    name = if (index == 0) "Legacy pack name" else definition.name,
                    content = definition.content,
                    price = definition.price,
                    status = definition.status,
                    now = now.minusSeconds(3_600),
                )
            }
        val existingId = existing.first().id
        val ownerUserId = UserId.generate()
        val fixture =
            fixture(
                initialRevision = 3,
                initialPersonalizations = existing,
                activeOwners =
                    mapOf(
                        existingId to listOf(PersonalizationOwner.User(ownerUserId)),
                    ),
            )

        fixture.service.execute()

        val updated = fixture.personalizationStore.byCatalogKey.getValue(firstDefinition.catalogKey)
        val payload =
            fixture.publisher.events
                .single()
                .payload as NotificationPackSelectionChangedV1

        assertEquals(existingId, updated.id)
        assertEquals(firstDefinition.name, updated.name)
        assertEquals(firstDefinition.content, updated.content)
        assertEquals(firstDefinition.price, updated.price)
        assertEquals(1, fixture.personalizationStore.saveCalls)
        assertEquals(1, fixture.stateRepository.updateCalls)
        assertEquals(4, fixture.stateRepository.state.revision)
        assertEquals(ownerUserId.id, payload.userId)
        assertEquals(existingId.id, payload.personalizationId)
        assertEquals(1L, payload.selectionRevision)
        assertEquals(now, payload.occurredAt)
    }

    @Test
    fun `does not let an older application instance downgrade newer catalog revision`() {
        val fixture = fixture(initialRevision = 5)

        fixture.service.execute()

        assertEquals(0, fixture.personalizationStore.lookupCalls)
        assertEquals(0, fixture.personalizationStore.saveCalls)
        assertEquals(0, fixture.stateRepository.updateCalls)
        assertEquals(5, fixture.stateRepository.state.revision)
        assertTrue(fixture.publisher.events.isEmpty())
    }

    private fun fixture(
        initialRevision: Int,
        initialPersonalizations: List<Personalization> = emptyList(),
        activeOwners: Map<PersonalizationId, List<PersonalizationOwner>> = emptyMap(),
    ): Fixture {
        val stateRepository = FakeCatalogStateRepository(initialRevision)
        val personalizationStore = FakePersonalizationStore(initialPersonalizations)
        val ownershipReader = FakeBuiltinPersonalizationOwnershipReader(activeOwners)
        val publisher = RecordingIntegrationEventPublisher()
        val selectionRevisionStore = IncrementingNotificationPackSelectionRevisionStore()
        val notificationPackSelectionEventPublisher =
            NotificationPackSelectionEventPublisher(
                publisher = publisher,
                revisionStore = selectionRevisionStore,
                clock = clock,
            )
        val profileAppearanceEventPublisher =
            ProfileAppearanceEventPublisher(
                publisher = publisher,
                ownershipRepository = EmptyOwnedPersonalizationRepository(),
                personalizationRepository = personalizationStore,
                revisionStore = IncrementingProfileAppearanceRevisionStore(),
                clock = clock,
            )
        val service =
            SynchronizeBuiltinPersonalizationCatalogService(
                stateRepository = stateRepository,
                personalizationRepository = personalizationStore,
                builtinPersonalizationLookup = personalizationStore,
                ownershipReader = ownershipReader,
                notificationPackSelectionEventPublisher = notificationPackSelectionEventPublisher,
                profileAppearanceEventPublisher = profileAppearanceEventPublisher,
                clock = clock,
            )

        return Fixture(
            service = service,
            stateRepository = stateRepository,
            personalizationStore = personalizationStore,
            publisher = publisher,
        )
    }

    private data class Fixture(
        val service: SynchronizeBuiltinPersonalizationCatalogService,
        val stateRepository: FakeCatalogStateRepository,
        val personalizationStore: FakePersonalizationStore,
        val publisher: RecordingIntegrationEventPublisher,
    )

    private class FakeCatalogStateRepository(
        initialRevision: Int,
    ) : BuiltinPersonalizationCatalogStateRepository {
        var state =
            BuiltinPersonalizationCatalogState(
                revision = initialRevision,
                updatedAt = Instant.EPOCH,
            )
        var updateCalls = 0

        override fun findForUpdate(): BuiltinPersonalizationCatalogState = state

        override fun update(
            revision: Int,
            updatedAt: Instant,
        ) {
            updateCalls += 1
            state =
                BuiltinPersonalizationCatalogState(
                    revision = revision,
                    updatedAt = updatedAt,
                )
        }
    }

    private class FakePersonalizationStore(
        initialPersonalizations: List<Personalization>,
    ) : PersonalizationRepository,
        BuiltinPersonalizationLookup {
        val byCatalogKey = linkedMapOf<PersonalizationCatalogKey, Personalization>()
        var lookupCalls = 0
        var saveCalls = 0

        init {
            initialPersonalizations.forEach { personalization ->
                byCatalogKey[requireNotNull(personalization.catalogKey)] = personalization
            }
        }

        override fun save(personalization: Personalization): Personalization {
            saveCalls += 1
            byCatalogKey[requireNotNull(personalization.catalogKey)] = personalization

            return personalization
        }

        override fun findById(id: PersonalizationId): Personalization? =
            byCatalogKey.values.firstOrNull { personalization -> personalization.id == id }

        override fun findByCatalogKeyForUpdate(catalogKey: PersonalizationCatalogKey): Personalization? {
            lookupCalls += 1

            return byCatalogKey[catalogKey]
        }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> = emptyList()

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> =
            byCatalogKey.values.filter { personalization -> personalization.status == status }

        override fun isAssetReferencedByPublicPersonalization(assetId: PersonalizationAssetId): Boolean = false

        override fun isAssetReferencedByReviewablePersonalization(assetId: PersonalizationAssetId): Boolean = false
    }

    private class FakeBuiltinPersonalizationOwnershipReader(
        private val owners: Map<PersonalizationId, List<PersonalizationOwner>>,
    ) : BuiltinPersonalizationOwnershipReader {
        override fun findActiveOwners(personalizationId: PersonalizationId): List<PersonalizationOwner> =
            owners[personalizationId].orEmpty()
    }

    private class IncrementingNotificationPackSelectionRevisionStore : NotificationPackSelectionRevisionStore {
        private val revisions = mutableMapOf<UserId, Long>()

        override fun next(userId: UserId): Long {
            val revision = (revisions[userId] ?: 0L) + 1L

            revisions[userId] = revision

            return revision
        }

        override fun current(userId: UserId): Long = revisions[userId] ?: 0L
    }

    private class IncrementingProfileAppearanceRevisionStore : ProfileAppearanceRevisionStore {
        private val revisions = mutableMapOf<AccountId, Long>()

        override fun next(accountId: AccountId): Long {
            val revision = (revisions[accountId] ?: 0L) + 1L

            revisions[accountId] = revision

            return revision
        }
    }

    private class EmptyOwnedPersonalizationRepository : OwnedPersonalizationRepository {
        override fun save(ownership: OwnedPersonalization): OwnedPersonalization = ownership

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

        override fun findActiveAccountIds(types: Set<PersonalizationType>): List<AccountId> = emptyList()

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) = Unit
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
        val MESSAGE_ID: UUID = UUID.fromString("019c113e-7e7d-7000-8000-000000000027")
    }
}
