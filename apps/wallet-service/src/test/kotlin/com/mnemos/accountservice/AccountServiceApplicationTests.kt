package com.mnemos.accountservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "mnemos.keycloak-admin.url=http://localhost:8080",
        "mnemos.keycloak-admin.realm=mnemos",
        "mnemos.cristalix-api.project-token=test-token",
        "mnemos.cristalix-api.project-key=test-key",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks",
        "spring.security.oauth2.client.registration.keycloak-admin.client-id=test-client",
        "spring.security.oauth2.client.registration.keycloak-admin.client-secret=test-secret",
        "spring.security.oauth2.client.registration.keycloak-admin.authorization-grant-type=client_credentials",
        "spring.security.oauth2.client.provider.keycloak-admin.token-uri=http://localhost:8080/realms/mnemos/protocol/openid-connect/token",
    ],
)
class AccountServiceApplicationTests {
    @Test
    fun contextLoads() {
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres =
            PostgreSQLContainer("postgres:17-alpine")
                .withDatabaseName("mnemos")
                .withUsername("mnemos")
                .withPassword("mnemos_test_password")
    }
}
