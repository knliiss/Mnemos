package com.mnemos.account.persistence.entity.user

import com.mnemos.common.core.model.user.LocalizationLanguage
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "telegram_id", nullable = false, unique = true, updatable = false)
    var telegramId: Long,
    @Column(name = "username", nullable = false)
    var username: String,
    @Column(name = "active_account_id")
    var activeAccountId: UUID?,
    @Column(name = "keycloak_id", nullable = false, unique = true, updatable = false)
    var keycloakId: UUID,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Enumerated(value = EnumType.STRING)
    @Column(name = "localization_language", nullable = false)
    var localizationLanguage: LocalizationLanguage,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
