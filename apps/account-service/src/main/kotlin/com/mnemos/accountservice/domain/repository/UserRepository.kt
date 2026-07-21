package com.mnemos.accountservice.domain.repository

import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.Username

interface UserRepository {
    fun create(user: User): User
    fun update(user: User): User
    fun findByTelegramId(telegramId: TelegramId): User?
    fun findByKeycloakId(keycloakId: KeycloakId): User?
    fun existsByUsername(username: Username): Boolean
    fun existsByTelegramId(telegramId: TelegramId): Boolean
}