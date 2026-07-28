package com.mnemos.accountservice.persistence.repository.account

import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryAdapter(
    private val jpaAccountRepository: JpaAccountRepository,
) : AccountRepository {
    override fun create(account: Account): Account = jpaAccountRepository.save(account.toEntity()).toDomain()

    override fun update(account: Account): Account {
        val entity =
            jpaAccountRepository
                .findById(account.id.value)
                .orElseThrow { AccountNotFoundException(account.id) }

        entity.updateFrom(account)
        return jpaAccountRepository.save(entity).toDomain()
    }

    override fun findActiveById(accountId: AccountId): Account? = jpaAccountRepository.findActiveById(accountId.value)?.toDomain()

    override fun existsByCristalixUuid(cristalixUuid: CristalixUuid): Boolean =
        jpaAccountRepository.existsByCristalixUuid(cristalixUuid.value)

    override fun findAllAvailableForUser(userId: UserId): List<Account> =
        jpaAccountRepository
            .findAllAvailableForUser(userId.value)
            .map { it.toDomain() }

    override fun lockById(id: AccountId): Boolean = jpaAccountRepository.findByIdForUpdate(id.value) != null

    override fun findByIdForUpdate(accountId: AccountId): Account? = jpaAccountRepository.findByIdForUpdate(accountId.value)?.toDomain()

    override fun findByIdIncludingInactive(accountId: AccountId): Account? =
        jpaAccountRepository
            .findById(accountId.value)
            .map { it.toDomain() }
            .orElse(null)
}
