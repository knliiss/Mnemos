package com.mnemos.accountservice.domain.repository

import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId

interface AccountRepository {
    fun create(account: Account): Account
    fun update(account: Account): Account
    fun findById(accountId: AccountId): Account?
    fun findByCristalixUuid(cristalixUuid: CristalixUuid): Account?
    fun existsByCristalixUuid(cristalixUuid: CristalixUuid): Boolean
    fun findAllAvailableForUser(userId: UserId): List<Account>
    fun existsById(accountId: AccountId): Boolean
    fun deleteById(id: AccountId)
}