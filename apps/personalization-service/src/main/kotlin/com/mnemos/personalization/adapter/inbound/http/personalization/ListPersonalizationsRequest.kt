package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ListPersonalizationsRequest(
    val type: PersonalizationType? = null,
    val currency: Currency? = null,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(PageQuery.MAX_PAGE_SIZE.toLong())
    val size: Int = 20,
)
