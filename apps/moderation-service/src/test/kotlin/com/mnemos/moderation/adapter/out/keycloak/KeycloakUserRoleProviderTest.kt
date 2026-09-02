package com.mnemos.moderation.adapter.out.keycloak

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.application.security.exception.UserRoleLookupUnavailableException
import feign.FeignException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class KeycloakUserRoleProviderTest {
    @Test
    fun `keycloak admin failure is exposed as service unavailable`() {
        val client = mock<KeycloakAdminClient>()
        val tokenProvider = mock<KeycloakTokenProvider>()
        val properties =
            KeycloakAdminProperties(
                url = "http://keycloak:8080",
                realm = "mnemos",
            )
        val provider =
            KeycloakUserRoleProvider(
                client = client,
                properties = properties,
                tokenProvider = tokenProvider,
            )
        val userId = UserId.generate()
        val keycloakFailure = mock<FeignException>()

        whenever(tokenProvider.token()).thenReturn("token")
        whenever(
            client.findUsers(
                authorization = eq("Bearer token"),
                realm = eq("mnemos"),
                query = any(),
            ),
        ).thenThrow(keycloakFailure)

        val exception =
            assertThrows(UserRoleLookupUnavailableException::class.java) {
                provider.highestRole(userId)
            }

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.httpStatus)
        assertEquals("USER_ROLE_LOOKUP_UNAVAILABLE", exception.code)
        assertEquals(keycloakFailure, exception.cause)
    }
}
