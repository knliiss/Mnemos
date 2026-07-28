package com.mnemos.account.persistence.repository.user

import com.mnemos.account.persistence.entity.user.UserEntity
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
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
    """,
    )
    fun existsByUsernameIgnoreCase(username: String): Boolean

    @Modifying
    @Query(
        """
            UPDATE UserEntity user
            SET user.activeAccountId = NULL,
                user.updatedAt = :updatedAt
            WHERE user.activeAccountId = :accountId
        """,
    )
    fun clearActiveAccountByAccountId(
        @Param("accountId") accountId: UUID,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Modifying(
        flushAutomatically = true,
        clearAutomatically = true,
    )
    @Query(
        """
        UPDATE UserEntity user
        SET user.activeAccountId = NULL, user.updatedAt = :updatedAt
        WHERE user.id = :userId AND user.activeAccountId = :accountId
    """,
    )
    fun clearActiveAccountIfMatches(
        @Param("userId") userId: UUID,
        @Param("accountId") accountId: UUID,
        @Param("updatedAt") updatedAt: Instant
    ): Int
}
