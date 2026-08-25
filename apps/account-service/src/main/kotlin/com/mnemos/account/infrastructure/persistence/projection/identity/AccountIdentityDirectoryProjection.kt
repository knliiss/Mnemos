package com.mnemos.account.infrastructure.persistence.projection.identity

import java.time.Instant
import java.util.UUID

interface AccountIdentityDirectoryProjection {
    fun getAccountId(): UUID

    fun getCristalixUuid(): UUID

    fun getCristalixNickname(): String

    fun getStatus(): String

    fun getOwnerUserId(): UUID

    fun getOwnerUsername(): String

    fun getCreatedAt(): Instant
}
