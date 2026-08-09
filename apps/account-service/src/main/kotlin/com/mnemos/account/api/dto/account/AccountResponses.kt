package com.mnemos.account.api.dto.account

import com.mnemos.account.application.query.account.AccountDetails
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.application.result.account.CreateAccountResult
import java.time.Instant
import java.util.UUID

data class CreateAccountResponse(
    val accountId: UUID,
    val memberId: UUID,
)

data class AccountSummaryResponse(
    val id: UUID,
    val cristalixNickname: String,
    val currentMemberId: UUID,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
)

data class AccountDetailsResponse(
    val id: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val currentMemberId: UUID,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

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
