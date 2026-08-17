package com.mnemos.personalization.application.personalization.usecase.listcustompacks

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.custom.port.out.CustomNotificationPackReader
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.personalization.model.Personalization
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListCustomNotificationPacksService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val reader: CustomNotificationPackReader,
) : ListCustomNotificationPacksUseCase {
    @Transactional(readOnly = true)
    override fun execute(page: PageQuery): PageResult<Personalization> {
        val actor = actorProvider.requireActor()

        return reader.findByCreatorUserId(
            creatorUserId = actor.userId,
            page = page,
        )
    }
}
