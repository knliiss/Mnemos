package com.mnemos.account.domain.repository.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

interface AccountRepository {
    fun create(account: Account): Account

    fun update(account: Account): Account

    fun findActiveById(accountId: AccountId): Account?

    fun findActiveByCristalixNickname(nickname: String): Account?

    fun findActiveByUserId(userId: UserId): Account?

    fun existsByCristalixUuid(cristalixUuid: CristalixUuid): Boolean

    fun findAllAvailableForUser(userId: UserId): List<Account>

    fun lockById(id: AccountId): Boolean

    fun findByIdForUpdate(accountId: AccountId): Account?

    fun findByIdIncludingInactive(accountId: AccountId): Account?
}
