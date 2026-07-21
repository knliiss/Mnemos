package com.mnemos.accountservice.persistence.repository

import com.mnemos.accountservice.domain.exception.UserNotFoundException
import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.accountservice.persistence.mapper.UserPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.UserPersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.UserPersistenceMapper.updateFrom
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.Username
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun create(user: User): User =
        jpaUserRepository.save(user.toEntity()).toDomain()

    override fun update(user: User): User {
        val entity = jpaUserRepository.findById(user.id.value)
            .orElseThrow { UserNotFoundException(user.keycloakId) }
        entity.updateFrom(user)
        return jpaUserRepository.saveAndFlush(entity).toDomain()
    }

    override fun findByTelegramId(telegramId: TelegramId): User? =
        jpaUserRepository.findByTelegramId(telegramId)?.toDomain()

    override fun findByKeycloakId(keycloakId: KeycloakId): User? =
        jpaUserRepository.findByKeycloakId(keycloakId)?.toDomain()

    override fun existsByTelegramId(telegramId: TelegramId): Boolean =
        jpaUserRepository.existsByTelegramId(telegramId)

    override fun existsByUsername(username: Username): Boolean =
        jpaUserRepository.existsByUsernameIgnoreCase(username.value)
}