package com.mnemos.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class MnemosJwtAuthenticationConverter(
    private val properties: MnemosSecurityProperties,
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        return JwtAuthenticationToken(
            jwt,
            extractRealmRoles(jwt),
            extractPrincipalName(jwt),
        )
    }

    private fun extractRealmRoles(
        jwt: Jwt,
    ): Collection<GrantedAuthority> {
        val realmAccess = jwt.getClaimAsMap("realm_access")
            ?: return emptyList()

        val roles = realmAccess["roles"] as? Collection<*>
            ?: return emptyList()

        return roles
            .filterIsInstance<String>()
            .map(String::trim)
            .filter(String::isNotBlank)
            .map(::normalizeRole)
            .distinct()
            .map(::SimpleGrantedAuthority)
    }

    private fun normalizeRole(role: String): String {
        val normalizedRole = role.uppercase()
        val normalizedPrefix = properties.rolePrefix.uppercase()

        return if (normalizedRole.startsWith(normalizedPrefix)) {
            normalizedRole
        } else {
            normalizedPrefix + normalizedRole
        }
    }

    private fun extractPrincipalName(jwt: Jwt): String =
        jwt.getClaimAsString(properties.principalClaim)
            ?.takeIf { it.isNotBlank() }
            ?: jwt.subject
                ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException(
                "JWT does not contain a valid principal claim"
            )
}