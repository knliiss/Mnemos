package com.mnemos.personalization.application.personalization.usecase.listcatalog

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

data class ListPersonalizationsQuery(
    val type: PersonalizationType,
    val currency: Currency?,
    val page: PageQuery,
)
