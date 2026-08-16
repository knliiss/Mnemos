package com.mnemos.account.api.controller.internal

import com.mnemos.account.domain.model.account.Account
import com.mnemos.contracts.account.internal.v1.InternalAccountResponse

object InternalAccountApiMapper {
    fun Account.toInternalResponse(): InternalAccountResponse =
        InternalAccountResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid.id,
            cristalixNickname = cristalixNickname.value,
        )
}
