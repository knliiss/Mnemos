package com.mnemos.personalization.adapter.out.persistence.ownership

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.stereotype.Component

@Component
class OwnedPersonalizationRepositoryAdapter(
    private val repository: OwnedPersonalizationJpaRepository,
) : OwnedPersonalizationRepository {
    override fun save(ownership: OwnedPersonalization): OwnedPersonalization {
        val existing =
            repository.findByOwnerTypeAndOwnerIdAndPersonalizationId(
                ownerType = ownership.owner.target,
                ownerId = ownership.owner.id,
                personalizationId = ownership.personalizationId.id,
            )

        val entity =
            OwnedPersonalizationEntity(
                id = existing?.id ?: UuidCreator.getTimeOrderedEpoch(),
                ownerType = ownership.owner.target,
                ownerId = ownership.owner.id,
                personalizationId = ownership.personalizationId.id,
                personalizationType = ownership.type,
                acquiredAt = ownership.acquiredAt,
                active = ownership.active,
            )

        return repository.saveAndFlush(entity).toDomain()
    }

    override fun exists(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): Boolean =
        repository.existsByOwnerTypeAndOwnerIdAndPersonalizationId(
            ownerType = owner.target,
            ownerId = owner.id,
            personalizationId = personalizationId.id,
        )

    override fun find(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization? =
        repository
            .findByOwnerTypeAndOwnerIdAndPersonalizationId(
                ownerType = owner.target,
                ownerId = owner.id,
                personalizationId = personalizationId.id,
            )
            ?.toDomain()

    override fun findForUpdate(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization? =
        repository
            .findByOwnerTypeAndOwnerIdAndPersonalizationIdForUpdate(
                ownerType = owner.target,
                ownerId = owner.id,
                personalizationId = personalizationId.id,
            )
            ?.toDomain()

    override fun findActive(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    ): OwnedPersonalization? =
        repository
            .findByOwnerTypeAndOwnerIdAndPersonalizationTypeAndActiveTrue(
                ownerType = owner.target,
                ownerId = owner.id,
                personalizationType = type,
            )
            ?.toDomain()

    override fun findActiveForUpdate(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    ): OwnedPersonalization? =
        repository
            .findActiveForUpdate(
                ownerType = owner.target,
                ownerId = owner.id,
                personalizationType = type,
            )
            ?.toDomain()

    override fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization> =
        repository
            .findAllByOwnerTypeAndOwnerIdOrderByAcquiredAtDesc(
                ownerType = owner.target,
                ownerId = owner.id,
            )
            .map { it.toDomain() }

    override fun deactivateActive(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    ) {
        repository.deactivateActive(
            ownerType = owner.target,
            ownerId = owner.id,
            personalizationType = type,
        )
    }

    private fun OwnedPersonalizationEntity.toDomain(): OwnedPersonalization =
        try {
            OwnedPersonalization(
                owner =
                    when (ownerType) {
                        PersonalizationTarget.USER -> PersonalizationOwner.User(UserId(ownerId))
                        PersonalizationTarget.ACCOUNT -> PersonalizationOwner.Account(AccountId(ownerId))
                    },
                personalizationId = PersonalizationId(personalizationId),
                type = personalizationType,
                acquiredAt = acquiredAt,
                active = active,
            )
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException(
                "Persisted personalization ownership $id contains invalid domain state",
                exception,
            )
        }
}
