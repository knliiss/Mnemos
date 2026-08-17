package com.mnemos.account.api.dto.account

import com.mnemos.account.application.query.account.AccountDetails
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.application.result.account.CreateAccountResult

object AccountApiMapper {
    fun CreateAccountResult.toResponse(): CreateAccountResponse =
        CreateAccountResponse(
            accountId = accountId.id,
            memberId = memberId.id,
        )

    fun AccountSummary.toResponse(): AccountSummaryResponse =
        AccountSummaryResponse(
            id = id.id,
            cristalixNickname = cristalixNickname,
            currentMemberId = currentMemberId.id,
            memberCount = memberCount,
            maxMembers = maxMembers,
            owner = owner,
            active = active,
        )

    fun AccountDetails.toResponse(): AccountDetailsResponse =
        AccountDetailsResponse(
            id = id.id,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            currentMemberId = currentMemberId.id,
            memberCount = memberCount,
            maxMembers = maxMembers,
            owner = owner,
            active = active,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
