package com.mnemos.account.api.dto.account.member

import com.mnemos.account.application.query.account.member.AccountMemberListItem
import com.mnemos.account.application.query.account.member.AccountMemberRoleSummary
import com.mnemos.account.application.service.member.JoinAccountResult

object AccountMemberApiMapper {
    fun AccountMemberListItem.toResponse(): AccountMemberListItemResponse =
        AccountMemberListItemResponse(
            id = id.id,
            name = name,
            owner = owner,
            highestRole = highestRole?.toResponse(),
            joinedAt = joinedAt,
        )

    private fun AccountMemberRoleSummary.toResponse(): AccountMemberRoleSummaryResponse =
        AccountMemberRoleSummaryResponse(
            id = id.id,
            name = name.value,
            color = color.value,
        )

    fun JoinAccountResult.toResponse(): JoinAccountResponse =
        JoinAccountResponse(
            accountId = accountId.id,
            memberId = memberId.id,
        )
}
