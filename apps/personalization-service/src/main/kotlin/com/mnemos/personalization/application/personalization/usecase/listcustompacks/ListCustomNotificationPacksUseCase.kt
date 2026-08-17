package com.mnemos.personalization.application.personalization.usecase.listcustompacks

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.Personalization

interface ListCustomNotificationPacksUseCase {
    fun execute(page: PageQuery): PageResult<Personalization>
}
