package com.mnemos.account.infrastructure.persistence.projection.identity

import java.util.UUID

interface IdentitySearchProjection {
    fun getUserId(): UUID

    fun getUsername(): String

    fun getTelegramId(): Long

    fun getAccountId(): UUID?

    fun getCristalixUuid(): UUID?

    fun getCristalixNickname(): String?

    fun getActive(): Boolean?
}
