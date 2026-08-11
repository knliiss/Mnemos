package com.mnemos.account.persistence.repository.account

import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.persistence.mapper.AccountPersistenceMapper.toDomain
import com.mnemos.account.persistence.mapper.AccountPersistenceMapper.toEntity
import com.mnemos.account.persistence.mapper.AccountPersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryAdapter(
    private val jpaAccountRepository: JpaAccountRepository,
) : AccountRepository {
    override fun create(account: Account): Account = jpaAccountRepository.save(account.toEntity()).toDomain()

    override fun update(account: Account): Account {
        val entity =
            jpaAccountRepository
                .findById(account.id.id)
                .orElseThrow { AccountNotFoundException(account.id) }

        entity.updateFrom(account)
        return jpaAccountRepository.save(entity).toDomain()
    }

    override fun findActiveById(accountId: AccountId): Account? = jpaAccountRepository.findActiveById(accountId.id)?.toDomain()

    override fun findActiveByCristalixNickname(nickname: String): Account? =
        jpaAccountRepository.findActiveByCristalixNickname(nickname)?.toDomain()

    override fun findActiveByUserId(userId: UserId): Account? = jpaAccountRepository.findActiveByUserId(userId.id)?.toDomain()

    override fun existsByCristalixUuid(cristalixUuid: CristalixUuid): Boolean = jpaAccountRepository.existsByCristalixUuid(cristalixUuid.id)

    override fun findAllAvailableForUser(userId: UserId): List<Account> =
        jpaAccountRepository
            .findAllAvailableForUser(userId.id)
            .map { it.toDomain() }

    override fun lockById(id: AccountId): Boolean = jpaAccountRepository.findByIdForUpdate(id.id) != null

    override fun findByIdForUpdate(accountId: AccountId): Account? = jpaAccountRepository.findByIdForUpdate(accountId.id)?.toDomain()

    override fun findByIdIncludingInactive(accountId: AccountId): Account? =
        jpaAccountRepository
            .findById(accountId.id)
            .map { it.toDomain() }
            .orElse(null)
}
