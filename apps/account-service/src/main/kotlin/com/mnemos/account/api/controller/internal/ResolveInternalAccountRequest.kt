package com.mnemos.account.api.controller.internal

import jakarta.validation.constraints.AssertTrue
import java.util.UUID

data class ResolveInternalAccountRequest(
    val accountId: UUID? = null,
    val userId: UUID? = null,
    val cristalixNickname: String? = null,
) {
    @get:AssertTrue(message = "Exactly one account lookup key must be provided")
    val hasExactlyOneLookupKey: Boolean
        get() = listOf(accountId, userId, cristalixNickname).count { value -> value != null } == 1
}
