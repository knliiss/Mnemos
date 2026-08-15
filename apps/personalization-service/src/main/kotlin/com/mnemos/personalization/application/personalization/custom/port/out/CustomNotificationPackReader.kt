package com.mnemos.personalization.application.personalization.custom.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.Personalization

interface CustomNotificationPackReader {
    fun findByCreatorUserId(
        creatorUserId: UserId,
        page: PageQuery,
    ): PageResult<Personalization>
}
