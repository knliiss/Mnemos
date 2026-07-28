package com.mnemos.account.persistence.repository.user

import com.mnemos.account.domain.exception.UserNotFoundException
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.persistence.mapper.UserPersistenceMapper.toDomain
import com.mnemos.account.persistence.mapper.UserPersistenceMapper.toEntity
import com.mnemos.account.persistence.mapper.UserPersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.core.model.user.Username
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class UserRepositoryAdapter(
    private val jpaUserRepository: JpaUserRepository,
) : UserRepository {
    override fun create(user: User): User = jpaUserRepository.save(user.toEntity()).toDomain()

    override fun update(user: User): User {
        val entity =
            jpaUserRepository
                .findById(user.id.value)
                .orElseThrow { UserNotFoundException(user.keycloakId) }
        entity.updateFrom(user)
        return jpaUserRepository.save(entity).toDomain()
    }

    override fun findByTelegramId(telegramId: TelegramId): User? = jpaUserRepository.findByTelegramId(telegramId)?.toDomain()

    override fun findByKeycloakId(keycloakId: KeycloakId): User? = jpaUserRepository.findByKeycloakId(keycloakId)?.toDomain()

    override fun existsByTelegramId(telegramId: TelegramId): Boolean = jpaUserRepository.existsByTelegramId(telegramId)

    override fun clearActiveAccountByAccountId(
        accountId: AccountId,
        now: Instant,
    ) {
        jpaUserRepository.clearActiveAccountByAccountId(accountId.value, now)
    }

    override fun clearActiveAccountIfMatches(
        userId: UserId,
        accountId: AccountId,
        updatedAt: Instant,
    ): Boolean =
        jpaUserRepository.clearActiveAccountIfMatches(
            userId = userId.value,
            accountId = accountId.value,
            updatedAt = updatedAt,
        ) > 0

    override fun existsByUsername(username: Username): Boolean = jpaUserRepository.existsByUsernameIgnoreCase(username.value)
}
