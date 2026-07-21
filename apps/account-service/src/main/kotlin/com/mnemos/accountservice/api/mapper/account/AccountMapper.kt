package com.mnemos.accountservice.api.mapper.account

import com.mnemos.accountservice.api.dto.account.AvailableAccountResponse
import com.mnemos.accountservice.api.dto.account.AccountResponse
import com.mnemos.accountservice.domain.model.account.Account

object AccountMapper {
    fun Account.toResponse() = AccountResponse(
        id = id.value,
        ownerUserId = ownerUserId.value,
        cristalixUuid = cristalixUuid.value,
        cristalixNickname = cristalixNickname.value,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun Account.toAvailableAccountResponse() = AvailableAccountResponse(
        id = id.value,
        ownerUserId = ownerUserId.value,
        cristalixUuid = cristalixUuid.value,
        cristalixNickname = cristalixNickname.value,
    )

}