package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.account.AccountLookupSummary
import com.mnemos.account.application.query.account.InternalAccountBootstrapQuery
import com.mnemos.account.application.query.account.ResolveInternalAccountQuery
import com.mnemos.account.domain.model.account.Account
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse
import com.mnemos.contracts.account.internal.v1.InternalAccountSummaryResponse

object InternalAccountApiMapper {
    fun ResolveInternalAccountRequest.toQuery(): ResolveInternalAccountQuery =
        when {
            accountId != null -> ResolveInternalAccountQuery.ByAccountId(AccountId(accountId))
            userId != null -> ResolveInternalAccountQuery.ByUserId(UserId(userId))
            else ->
                ResolveInternalAccountQuery.ByCristalixNickname(
                    cristalixNickname = requireNotNull(cristalixNickname),
                )
        }

    fun InternalAccountBootstrapRequest.toQuery(): InternalAccountBootstrapQuery =
        InternalAccountBootstrapQuery(
            afterAccountId = afterAccountId?.let(::AccountId),
            limit = limit,
        )

    fun Account.toInternalResponse(): InternalAccountResponse =
        InternalAccountResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid.id,
            cristalixNickname = cristalixNickname.value,
        )

    fun Account.toBootstrapResponse(): InternalAccountBootstrapResponse =
        InternalAccountBootstrapResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid.id,
            cristalixNickname = cristalixNickname.value,
            createdAt = createdAt,
        )

    fun AccountLookupSummary.toSummaryResponse(): InternalAccountSummaryResponse =
        InternalAccountSummaryResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
        )
}
