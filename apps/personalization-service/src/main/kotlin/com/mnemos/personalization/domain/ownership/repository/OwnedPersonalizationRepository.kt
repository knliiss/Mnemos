package com.mnemos.personalization.domain.ownership.repository

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

    fun findActive(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    ): OwnedPersonalization?

    fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization>

    fun deactivateActive(
        owner: PersonalizationOwner,
        type: PersonalizationType,
    )
}
