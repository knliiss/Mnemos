package com.mnemos.contracts.account.v1

import java.util.UUID

data class AccountCreatedV1(
    val ownerMemberId: UUID,
    val ownerUserId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val maxMembers: Int
)