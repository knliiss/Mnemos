package com.mnemos.personalization.domain.ownership.repository

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

interface OwnedPersonalizationRepository {
    fun save(ownership: OwnedPersonalization): OwnedPersonalization

    fun exists(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): Boolean

    fun find(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization?

    fun findForUpdate(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization? = find(owner, personalizationId)

    fun findActive(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    ): OwnedPersonalization?

    fun findActiveForUpdate(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    ): OwnedPersonalization? = findActive(owner, type)

    fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization>

    fun findActiveAccountIds(types: Set<PersonalizationType>): List<AccountId>

    fun deactivateActive(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    )

    fun delete(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    )
}
