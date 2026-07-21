package com.mnemos.accountservice.domain.model.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import java.time.Instant

class Account private constructor(
    val id: AccountId,
    val ownerUserId: UserId,
    val cristalixUuid: CristalixUuid,

    cristalixNickname: CristalixNickname,

    val createdAt: Instant,
    updatedAt: Instant,
) {

    var cristalixNickname: CristalixNickname = cristalixNickname
        private set
    var updatedAt: Instant = updatedAt
        private set

    companion object {
        fun create(
            ownerUserId: UserId,
            cristalixUuid: CristalixUuid,
            cristalixNickname: CristalixNickname,
        ): Account {
            val now = Instant.now()
            return Account(
                id = AccountId.generate(),
                ownerUserId = ownerUserId,
                cristalixUuid = cristalixUuid,
                cristalixNickname = cristalixNickname,
                createdAt = now,
                updatedAt = now
            )
        }

        fun restore(
            id: AccountId,
            ownerUserId: UserId,
            cristalixUuid: CristalixUuid,
            cristalixNickname: CristalixNickname,
            createdAt: Instant,
            updatedAt: Instant,
        ): Account {
            return Account(
                id = id,
                ownerUserId = ownerUserId,
                cristalixUuid = cristalixUuid,
                cristalixNickname = cristalixNickname,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    fun updateCristalixNickname(newCristalixNickname: CristalixNickname) {
        if (newCristalixNickname == cristalixNickname) return
        cristalixNickname = newCristalixNickname
        updatedAt = Instant.now()
    }
}