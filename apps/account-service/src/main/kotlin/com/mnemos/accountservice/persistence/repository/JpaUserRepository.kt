package com.mnemos.accountservice.persistence.repository

import com.mnemos.accountservice.persistence.entity.UserEntity
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaUserRepository : JpaRepository<UserEntity, UUID> {
    fun findByTelegramId(telegramId: TelegramId): UserEntity?
    fun findByKeycloakId(keycloakId: KeycloakId): UserEntity?
    fun existsByTelegramId(telegramId: TelegramId): Boolean
    fun existsByKeycloakId(keycloakId: KeycloakId): Boolean

    @Query(
        """
    select case when count(user) > 0 then true else false end
    from UserEntity user
    where lower(user.username) = lower(:username)
    """
    )
    fun existsByUsernameIgnoreCase(username: String): Boolean

}