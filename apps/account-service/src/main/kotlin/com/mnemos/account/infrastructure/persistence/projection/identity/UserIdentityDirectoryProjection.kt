package com.mnemos.account.infrastructure.persistence.projection.identity

import java.time.Instant
import java.util.UUID

interface UserIdentityDirectoryProjection {
    fun getUserId(): UUID

    fun getUsername(): String

    fun getTelegramId(): Long

    fun getActiveAccountId(): UUID?

    fun getAccountCount(): Long

    fun getCreatedAt(): Instant
}
