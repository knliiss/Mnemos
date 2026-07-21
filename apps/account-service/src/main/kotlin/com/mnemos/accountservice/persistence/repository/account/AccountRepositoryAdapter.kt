package com.mnemos.accountservice.persistence.repository.account

import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.domain.repository.AccountRepository
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryAdapter(
    private val jpaAccountRepository: JpaAccountRepository
) : AccountRepository {

    override fun create(account: Account): Account =
        jpaAccountRepository.save(account.toEntity()).toDomain()

    override fun update(account: Account): Account {
        val entity = jpaAccountRepository.findById(account.id.value)
            .orElseThrow { AccountNotFoundException(account.id) }

        entity.updateFrom(account)

        return jpaAccountRepository.saveAndFlush(entity).toDomain()
    }

    override fun findById(accountId: AccountId): Account? =
        jpaAccountRepository.findById(accountId.value)
            .map { it.toDomain() }
            .orElse(null)

    override fun findByCristalixUuid(cristalixUuid: CristalixUuid): Account? =
        jpaAccountRepository.findByCristalixUuid(cristalixUuid.value)
            ?.toDomain()

    override fun existsByCristalixUuid(cristalixUuid: CristalixUuid): Boolean =
        jpaAccountRepository.existsByCristalixUuid(cristalixUuid.value)

    override fun findAllAvailableForUser(userId: UserId): List<Account> {
        return jpaAccountRepository.findAllAvailableForUser(userId.value)
            .map { it.toDomain() }
    }

    override fun existsById(accountId: AccountId): Boolean {
        return jpaAccountRepository.existsById(accountId.value)
    }

    override fun deleteById(id: AccountId) {
        return jpaAccountRepository.deleteById(id.value)
    }
}