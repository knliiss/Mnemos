package com.mnemos.account.application.service.user

import com.mnemos.account.domain.exception.user.UserIdentityConflictException
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import org.springframework.stereotype.Service

@Service
class TelegramUsernameResolver(
    private val userRepository: UserRepository,
) {
    fun resolve(
        telegramId: TelegramId,
        telegramUsername: String?,
    ): TelegramUsernameResolution {
        val primary = Username(resolvePrimaryValue(telegramId, telegramUsername))
        val collision = Username("${primary.value}_${telegramId.id}")

        if (!userRepository.existsByUsername(primary)) {
            return TelegramUsernameResolution(
                selected = primary,
                keycloakFallback = collision.takeUnless(userRepository::existsByUsername),
            )
        }

        if (!userRepository.existsByUsername(collision)) {
            return TelegramUsernameResolution(
                selected = collision,
                keycloakFallback = null,
            )
        }

        throw UserIdentityConflictException(
            message = "No Telegram-derived username is available",
            details =
                mapOf(
                    "telegramId" to telegramId.id,
                    "telegramUsername" to telegramUsername.orEmpty(),
                ),
        )
    }

    private fun resolvePrimaryValue(
        telegramId: TelegramId,
        telegramUsername: String?,
    ): String =
        telegramUsername
            ?.trim()
            ?.removePrefix("@")
            ?.takeIf(::isValidTelegramUsername)
            ?: telegramId.id.toString()

    private fun isValidTelegramUsername(username: String): Boolean =
        username.length in TELEGRAM_USERNAME_MIN_LENGTH..TELEGRAM_USERNAME_MAX_LENGTH &&
            TELEGRAM_USERNAME_PATTERN.matches(username)

    private companion object {
        const val TELEGRAM_USERNAME_MIN_LENGTH = 3
        const val TELEGRAM_USERNAME_MAX_LENGTH = 32
        val TELEGRAM_USERNAME_PATTERN = Regex("[A-Za-z0-9_]+")
    }
}

data class TelegramUsernameResolution(
    val selected: Username,
    val keycloakFallback: Username?,
)
