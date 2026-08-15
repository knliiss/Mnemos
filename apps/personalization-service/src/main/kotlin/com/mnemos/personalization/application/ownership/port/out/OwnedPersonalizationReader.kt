package com.mnemos.personalization.application.ownership.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

interface OwnedPersonalizationReader {
    fun findOwned(
        userId: UserId,
        accountId: AccountId?,
        type: PersonalizationType?,
        page: PageQuery,
    ): PageResult<OwnedPersonalization>
}
