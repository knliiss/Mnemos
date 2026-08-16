package com.mnemos.account.api.controller.internal

import com.mnemos.account.domain.model.account.Account
import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse

object InternalAccountApiMapper {
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
}
