package com.mnemos.personalization.application.ownership.usecase.listmine

import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.ownership.port.out.OwnedPersonalizationReader
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListOwnedPersonalizationsService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val reader: OwnedPersonalizationReader,
    private val support: PersonalizationOwnershipSupport,
) : ListOwnedPersonalizationsUseCase {
    @Transactional(readOnly = true)
    override fun execute(query: ListOwnedPersonalizationsQuery): PageResult<OwnedPersonalization> {
        val actor = actorProvider.requireActor()
        val accountId = support.resolveReadableAccountId(actor.userId)

        return reader.findOwned(
            userId = actor.userId,
            accountId = accountId,
            type = query.type,
            page = query.page,
        )
    }
}
