package com.mnemos.personalization.application.personalization.builtin.port.out

import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId

interface BuiltinPersonalizationOwnershipReader {
    fun findActiveOwners(personalizationId: PersonalizationId): List<PersonalizationOwner>
}
