package com.mnemos.accountservice.api.dto.account.member

import com.mnemos.accountservice.application.query.account.member.AccountMemberListItem
import com.mnemos.accountservice.application.query.account.member.AccountMemberRoleSummary
import com.mnemos.accountservice.application.service.member.JoinAccountResult
import java.time.Instant
import java.util.UUID

data class AccountMemberListItemResponse(
    val id: UUID,
    val name: String,
    val owner: Boolean,
    val highestRole: AccountMemberRoleSummaryResponse?,
    val joinedAt: Instant,
)

data class AccountMemberRoleSummaryResponse(
    val id: UUID,
    val name: String,
    val color: String,
)

data class JoinAccountResponse(
    val accountId: UUID,
    val memberId: UUID,
)

fun AccountMemberListItem.toResponse(): AccountMemberListItemResponse =
    AccountMemberListItemResponse(
        id = id.value,
        name = name,
        owner = owner,
        highestRole = highestRole?.toResponse(),
        joinedAt = joinedAt,
    )

private fun AccountMemberRoleSummary.toResponse(): AccountMemberRoleSummaryResponse =
    AccountMemberRoleSummaryResponse(
        id = id.value,
        name = name.value,
        color = color.value,
    )

fun JoinAccountResult.toResponse(): JoinAccountResponse =
    JoinAccountResponse(
        accountId = accountId.value,
        memberId = memberId.value,
    )
