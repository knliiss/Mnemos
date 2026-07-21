package com.mnemos.security

import com.mnemos.common.core.model.user.UserRole
import com.mnemos.security.exception.InvalidAuthenticationPrincipalException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.UUID

class CurrentMnemosUser(
    private val properties: MnemosSecurityProperties,
    private val logger: Logger = LoggerFactory.getLogger(CurrentMnemosUser::class.java)
) {

    fun get(): MnemosPrincipal {
        val authentication = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken
            ?: throw InvalidAuthenticationPrincipalException(
                message = "Current authentication is not JWT"
            )

        val jwt = authentication.token

        return MnemosPrincipal(
            keycloakId = extractKeycloakId(jwt),
            username = extractUsername(jwt),
            telegramId = extractTelegramId(jwt),
            roles = extractRoles(authentication)
        )
    }

    private fun extractKeycloakId(jwt: Jwt): UUID {
        val subject = jwt.subject
            ?.takeIf { it.isNotBlank() }
            ?: throw InvalidAuthenticationPrincipalException(
                message = "JWT subject is missing"
            )

        return try {
            UUID.fromString(subject)
        } catch (exception: IllegalArgumentException) {
            throw InvalidAuthenticationPrincipalException(
                message = "JWT subject is not a valid UUID",
                cause = exception
            )
        }
    }

    private fun extractUsername(jwt: Jwt): String {
        return jwt.getClaimAsString("preferred_username")
            ?.takeIf { it.isNotBlank() }
            ?: throw InvalidAuthenticationPrincipalException(
                message = "JWT preferred_username claim is missing"
            )
    }

    private fun extractTelegramId(jwt: Jwt): Long {
        val raw = jwt.claims["telegram_id"]
            ?: throw InvalidAuthenticationPrincipalException(
                message = "Required JWT claim telegram_id is missing"
            )

        return when (raw) {
            is Number -> raw.toLong()

            is String -> raw.toLongOrNull()
                ?: throw InvalidAuthenticationPrincipalException(
                    message = "JWT claim telegram_id is not a valid Long"
                )

            else -> throw InvalidAuthenticationPrincipalException(
                message = "JWT claim telegram_id has unsupported type",
                details = mapOf(
                    "claimType" to raw::class.qualifiedName.orEmpty()
                )
            )
        }
    }

    private fun extractRoles(authentication: JwtAuthenticationToken): Set<UserRole> {
        val normalizedPrefix = properties.rolePrefix.trim().uppercase()

        val roles = authentication.authorities
            .mapNotNull { authority ->
                val authorityValue = authority.authority
                    ?.trim()
                    ?.uppercase()
                    ?: return@mapNotNull null

                val roleName = authorityValue
                    .removePrefix(normalizedPrefix)
                    .takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null

                UserRole.entries.firstOrNull { it.name == roleName }
            }
            .toSet()

        if (roles.isEmpty()) {
            logger.warn("Authenticated user does not have any Mnemos roles.\n" +
                    "Authorities: {}", authentication.authorities)
            throw InvalidAuthenticationPrincipalException(
                message = "Authenticated user does not have any Mnemos roles"
            )
        }

        return roles
    }
}