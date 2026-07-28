package com.mnemos.account.application.context

import com.mnemos.account.domain.exception.UserNotFoundException
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.security.CurrentMnemosUser
import org.jboss.logging.Logger
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider(
    private val currentMnemosUser: CurrentMnemosUser,
    private val userRepository: UserRepository,
    private val logger: Logger = Logger.getLogger(CurrentUserProvider::class.java.name),
) {
    fun get(): User {
        val keycloakId = KeycloakId(currentMnemosUser.get().keycloakId)
        val user = userRepository.findByKeycloakId(keycloakId)
        if (user == null) {
            logger.warn("User with ID $keycloakId not found")
            throw UserNotFoundException(keycloakId = keycloakId)
        }
        return user
    }
}
