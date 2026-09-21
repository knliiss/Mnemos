package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationCatalogStateRepository
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationLookup
import com.mnemos.personalization.application.personalization.builtin.port.out.BuiltinPersonalizationOwnershipReader
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class SynchronizeBuiltinPersonalizationCatalogService(
    private val stateRepository: BuiltinPersonalizationCatalogStateRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val builtinPersonalizationLookup: BuiltinPersonalizationLookup,
    private val ownershipReader: BuiltinPersonalizationOwnershipReader,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val notificationPackSelectionEventPublisher: NotificationPackSelectionEventPublisher,
    private val profileAppearanceEventPublisher: ProfileAppearanceEventPublisher,
    private val clock: Clock,
) {
    @Transactional
    fun execute() {
        val state = stateRepository.findForUpdate()

        if (state.revision > CATALOG_REVISION) {
            logger.info(
                "Skipping built-in personalization catalog synchronization because database revision {} is newer than application revision {}",
                state.revision,
                CATALOG_REVISION,
            )
            return
        }

        val now = clock.instant()
        val changedPersonalizations =
            definitions.mapNotNull { definition ->
                synchronizeDefinition(
                    definition = definition,
                    now = now,
                )
            }

        retireRemovedBuiltins(now)
        changedPersonalizations.forEach(::republishActiveSnapshots)

        if (state.revision != CATALOG_REVISION || changedPersonalizations.isNotEmpty()) {
            stateRepository.update(
                revision = CATALOG_REVISION,
                updatedAt = now,
            )
        }

        logger.info(
            "Built-in personalization catalog synchronized: revision={}, definitions={}, changed={}",
            CATALOG_REVISION,
            definitions.size,
            changedPersonalizations.size,
        )
    }

    private fun synchronizeDefinition(
        definition: BuiltinPersonalizationDefinition,
        now: Instant,
    ): Personalization? {
        val existing = builtinPersonalizationLookup.findByCatalogKeyForUpdate(definition.catalogKey)

        if (existing == null) {
            return personalizationRepository.save(
                Personalization.createSystem(
                    catalogKey = definition.catalogKey,
                    type = definition.type,
                    name = definition.name,
                    content = definition.content,
                    price = definition.price,
                    status = definition.status,
                    now = now,
                ),
            )
        }

        require(existing.origin == PersonalizationOrigin.SYSTEM) {
            "Built-in catalog key ${definition.catalogKey.value} is occupied by non-system personalization"
        }
        require(existing.type == definition.type) {
            "Built-in catalog key ${definition.catalogKey.value} cannot change personalization type"
        }

        val changed =
            existing.synchronizeSystem(
                name = definition.name,
                content = definition.content,
                price = definition.price,
                desiredStatus = definition.status,
                now = now,
            )

        if (!changed) {
            return null
        }

        return personalizationRepository.save(existing)
    }

    private fun retireRemovedBuiltins(now: Instant) {
        RETIRED_CATALOG_KEYS.forEach { catalogKey ->
            val personalization =
                builtinPersonalizationLookup.findByCatalogKeyForUpdate(
                    PersonalizationCatalogKey(catalogKey),
                ) ?: return@forEach

            require(personalization.origin == PersonalizationOrigin.SYSTEM) {
                "Retired built-in catalog key $catalogKey is occupied by non-system personalization"
            }

            val activeOwners = ownershipReader.findActiveOwners(personalization.id)
            activeOwners.forEach { owner ->
                ownershipRepository.deactivateActive(
                    owner = owner,
                    type = personalization.type,
                )
                if (owner is PersonalizationOwner.User) {
                    notificationPackSelectionEventPublisher.publishCleared(UserId(owner.id))
                }
            }

            val changed =
                personalization.synchronizeSystem(
                    name = personalization.name,
                    content = personalization.content,
                    price = requireNotNull(personalization.price),
                    desiredStatus = PersonalizationStatus.UNLISTED,
                    now = now,
                )

            if (changed) {
                personalizationRepository.save(personalization)
            }
        }
    }

    private fun republishActiveSnapshots(personalization: Personalization) {
        ownershipReader
            .findActiveOwners(personalization.id)
            .forEach { owner ->
                when (owner) {
                    is PersonalizationOwner.User -> republishUserSnapshot(owner, personalization)
                    is PersonalizationOwner.Account -> republishAccountSnapshot(owner, personalization)
                }
            }
    }

    private fun republishUserSnapshot(
        owner: PersonalizationOwner.User,
        personalization: Personalization,
    ) {
        require(personalization.type.target == PersonalizationTarget.USER) {
            "User-owned built-in personalization ${personalization.id.id} has account target"
        }

        notificationPackSelectionEventPublisher.publishSelected(
            userId = UserId(owner.id),
            personalization = personalization,
        )
    }

    private fun republishAccountSnapshot(
        owner: PersonalizationOwner.Account,
        personalization: Personalization,
    ) {
        require(personalization.type.target == PersonalizationTarget.ACCOUNT) {
            "Account-owned built-in personalization ${personalization.id.id} has user target"
        }

        profileAppearanceEventPublisher.publish(
            accountId = AccountId(owner.id),
            actorUserId = null,
        )
    }

    private companion object {
        val logger = LoggerFactory.getLogger(SynchronizeBuiltinPersonalizationCatalogService::class.java)

        val RETIRED_CATALOG_KEYS = setOf("notification.classic")

        const val CATALOG_REVISION = 8

        val definitions = BuiltinPersonalizationCatalog.definitions
    }
}
