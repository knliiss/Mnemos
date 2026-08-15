package com.mnemos.personalization.domain.personalization.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus

interface PersonalizationRepository {
    fun save(personalization: Personalization): Personalization

    fun findById(id: PersonalizationId): Personalization?

    fun findByCreatorUserId(creatorUserId: UserId): List<Personalization>

    fun findByStatus(status: PersonalizationStatus): List<Personalization>
}
