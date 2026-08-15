package com.mnemos.personalization.domain.ownership.model

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import java.util.UUID

sealed interface PersonalizationOwner {
    val id: UUID
    val target: PersonalizationTarget

    data class User(
        val userId: UserId,
    ) : PersonalizationOwner {
        override val id: UUID = userId.id
        override val target = PersonalizationTarget.USER
    }

    data class Account(
        val accountId: AccountId,
    ) : PersonalizationOwner {
        override val id: UUID = accountId.id
        override val target = PersonalizationTarget.ACCOUNT
    }
}
