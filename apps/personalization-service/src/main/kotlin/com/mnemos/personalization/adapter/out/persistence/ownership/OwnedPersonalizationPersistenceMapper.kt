package com.mnemos.personalization.adapter.out.persistence.ownership

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget

object OwnedPersonalizationPersistenceMapper {
    fun OwnedPersonalizationEntity.toDomain(): OwnedPersonalization =
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
