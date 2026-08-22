package com.mnemos.contracts.account.internal.v1

import java.util.UUID

data class InternalAccountMemberUserIdsResponse(
    val userIds: List<UUID>,
)
