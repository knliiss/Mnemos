package com.mnemos.accountservice.api

import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.accountservice.integration.keycloak.client.KeycloakAdminClient
import com.mnemos.accountservice.integration.keycloak.dto.KeycloakUserResponse
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.UUID

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CurrentUserControllerIntegrationTest {

    companion object {

        @JvmStatic
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("mnemos")
            .withUsername("mnemos")
            .withPassword("mnemos_test_password")
    }

    @MockitoBean
    lateinit var keycloakAdminClient: KeycloakAdminClient

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `should update current user username`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val telegramId = TelegramId(randomTelegramId())
        val oldUsername = Username(uniqueUsername("old"))
        val newUsername = uniqueUsername("new")

        val user = createUser(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = oldUsername
        )

        val keycloakUser = KeycloakUserResponse(
            username = oldUsername.value,
            enabled = true,
            firstName = "Test",
            lastName = "User"
        )

        Mockito.`when`(
            keycloakAdminClient.getUser(
                realm = "mnemos",
                userId = keycloakId.value
            )
        ).thenReturn(keycloakUser)

        performUsernameUpdate(
            keycloakId = keycloakId,
            telegramId = telegramId,
            currentUsername = oldUsername.value,
            newUsername = newUsername
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(newUsername))

        val updatedUser = userRepository.findByKeycloakId(keycloakId)

        requireNotNull(updatedUser) {
            "Updated user was not found"
        }

        assertEquals(newUsername, updatedUser.username.value)

        Mockito.verify(keycloakAdminClient).getUser(
            realm = "mnemos",
            userId = keycloakId.value
        )

        Mockito.verify(keycloakAdminClient).updateUser(
            realm = "mnemos",
            userId = keycloakId.value,
            request = keycloakUser.copy(username = newUsername)
        )

        assertEquals(oldUsername, user.username)
    }

    @Test
    fun `should return current user when username has not changed`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val telegramId = TelegramId(randomTelegramId())
        val username = Username(uniqueUsername("same"))

        createUser(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = username
        )

        performUsernameUpdate(
            keycloakId = keycloakId,
            telegramId = telegramId,
            currentUsername = username.value,
            newUsername = username.value
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(username.value))

        val storedUser = userRepository.findByKeycloakId(keycloakId)

        requireNotNull(storedUser)

        assertEquals(username, storedUser.username)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return 404 when current user does not exist`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val telegramId = TelegramId(randomTelegramId())

        performUsernameUpdate(
            keycloakId = keycloakId,
            telegramId = telegramId,
            currentUsername = uniqueUsername("missing"),
            newUsername = uniqueUsername("new")
        )
            .andExpect(status().isNotFound)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return 409 when username is already used by another user`() {
        val currentKeycloakId = KeycloakId(UUID.randomUUID())
        val currentTelegramId = TelegramId(randomTelegramId())

        val otherKeycloakId = KeycloakId(UUID.randomUUID())
        val otherTelegramId = TelegramId(randomTelegramId())

        val currentUsername = Username(uniqueUsername("current"))
        val takenUsername = Username(uniqueUsername("taken"))

        createUser(
            keycloakId = currentKeycloakId,
            telegramId = currentTelegramId,
            username = currentUsername
        )

        createUser(
            keycloakId = otherKeycloakId,
            telegramId = otherTelegramId,
            username = takenUsername
        )

        performUsernameUpdate(
            keycloakId = currentKeycloakId,
            telegramId = currentTelegramId,
            currentUsername = currentUsername.value,
            newUsername = takenUsername.value
        )
            .andExpect(status().isConflict)

        val currentUser = userRepository.findByKeycloakId(currentKeycloakId)

        requireNotNull(currentUser)

        assertEquals(currentUsername, currentUser.username)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should check username conflict without case sensitivity`() {
        val currentKeycloakId = KeycloakId(UUID.randomUUID())
        val currentTelegramId = TelegramId(randomTelegramId())

        val otherKeycloakId = KeycloakId(UUID.randomUUID())
        val otherTelegramId = TelegramId(randomTelegramId())

        val currentUsername = Username(uniqueUsername("current"))
        val takenUsername = uniqueUsername("taken")

        createUser(
            keycloakId = currentKeycloakId,
            telegramId = currentTelegramId,
            username = currentUsername
        )

        createUser(
            keycloakId = otherKeycloakId,
            telegramId = otherTelegramId,
            username = Username(takenUsername.lowercase())
        )

        performUsernameUpdate(
            keycloakId = currentKeycloakId,
            telegramId = currentTelegramId,
            currentUsername = currentUsername.value,
            newUsername = takenUsername.uppercase()
        )
            .andExpect(status().isConflict)

        val currentUser = userRepository.findByKeycloakId(currentKeycloakId)

        requireNotNull(currentUser)

        assertEquals(currentUsername, currentUser.username)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return 400 when username is blank`() {
        performUsernameUpdate(
            keycloakId = KeycloakId(UUID.randomUUID()),
            telegramId = TelegramId(randomTelegramId()),
            currentUsername = uniqueUsername("current"),
            newUsername = ""
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return 400 when username exceeds maximum length`() {
        performUsernameUpdate(
            keycloakId = KeycloakId(UUID.randomUUID()),
            telegramId = TelegramId(randomTelegramId()),
            currentUsername = uniqueUsername("current"),
            newUsername = "a".repeat(256)
        )
            .andExpect(status().isBadRequest)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return 401 when jwt is missing`() {
        mockMvc.perform(
            patch("/api/v1/users/me/username")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "${uniqueUsername("new")}"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isUnauthorized)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should rollback local username when keycloak update fails`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val telegramId = TelegramId(randomTelegramId())
        val oldUsername = Username(uniqueUsername("old"))
        val newUsername = uniqueUsername("new")

        createUser(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = oldUsername
        )

        val keycloakUser = KeycloakUserResponse(
            username = oldUsername.value,
            enabled = true
        )

        Mockito.`when`(
            keycloakAdminClient.getUser(
                realm = "mnemos",
                userId = keycloakId.value
            )
        ).thenReturn(keycloakUser)

        Mockito.doThrow(RuntimeException("Keycloak update failed"))
            .`when`(keycloakAdminClient)
            .updateUser(
                realm = "mnemos",
                userId = keycloakId.value,
                request = keycloakUser.copy(username = newUsername)
            )

        performUsernameUpdate(
            keycloakId = keycloakId,
            telegramId = telegramId,
            currentUsername = oldUsername.value,
            newUsername = newUsername
        )
            .andExpect(status().isInternalServerError)

        val storedUser = userRepository.findByKeycloakId(keycloakId)

        requireNotNull(storedUser)

        assertEquals(oldUsername, storedUser.username)

        Mockito.verify(keycloakAdminClient).updateUser(
            realm = "mnemos",
            userId = keycloakId.value,
            request = keycloakUser.copy(username = newUsername)
        )
    }

    @Test
    fun `should update current user localization`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val telegramId = TelegramId(randomTelegramId())
        val username = Username(uniqueUsername("locale"))

        createUser(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = username
        )

        performLocalizationUpdate(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = username.value,
            localizationLanguage = LocalizationLanguage.UK
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.localizationLanguage").value("UK"))

        val updatedUser = userRepository.findByKeycloakId(keycloakId)

        requireNotNull(updatedUser)

        assertEquals(
            LocalizationLanguage.UK,
            updatedUser.localizationLanguage
        )

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return current user when localization has not changed`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val telegramId = TelegramId(randomTelegramId())
        val username = Username(uniqueUsername("locale"))

        createUser(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = username
        )

        performLocalizationUpdate(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = username.value,
            localizationLanguage = LocalizationLanguage.EN
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.localizationLanguage").value("EN"))

        val storedUser = userRepository.findByKeycloakId(keycloakId)

        requireNotNull(storedUser)

        assertEquals(
            LocalizationLanguage.EN,
            storedUser.localizationLanguage
        )

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    @Test
    fun `should return 404 when updating localization of missing user`() {
        performLocalizationUpdate(
            keycloakId = KeycloakId(UUID.randomUUID()),
            telegramId = TelegramId(randomTelegramId()),
            username = uniqueUsername("missing"),
            localizationLanguage = LocalizationLanguage.UK
        )
            .andExpect(status().isNotFound)

        Mockito.verifyNoInteractions(keycloakAdminClient)
    }

    private fun createUser(
        keycloakId: KeycloakId,
        telegramId: TelegramId,
        username: Username
    ): User {
        val user = User.create(
            keycloakId = keycloakId,
            telegramId = telegramId,
            username = username
        )

        return userRepository.create(user)
    }

    private fun performUsernameUpdate(
        keycloakId: KeycloakId,
        telegramId: TelegramId,
        currentUsername: String,
        newUsername: String
    ) = mockMvc.perform(
        patch("/api/v1/users/me/username")
            .with(
                jwt()
                    .jwt { token ->
                        token
                            .subject(keycloakId.value.toString())
                            .claim(
                                "preferred_username",
                                currentUsername
                            )
                            .claim(
                                "telegram_id",
                                telegramId.value
                            )
                    }
                    .authorities(
                        SimpleGrantedAuthority("ROLE_USER")
                    )
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {
                  "username": "$newUsername"
                }
                """.trimIndent()
            )
    )

    private fun uniqueUsername(prefix: String): String {
        val suffix = UUID.randomUUID().toString().substring(0, 8).replace("-", "")
        return "${prefix}_${suffix}"
    }

    private fun randomTelegramId(): Long {
        return UUID.randomUUID()
            .mostSignificantBits
            .let(Math::abs)
            .coerceAtLeast(1)
    }

    private fun performLocalizationUpdate(
        keycloakId: KeycloakId,
        telegramId: TelegramId,
        username: String,
        localizationLanguage: LocalizationLanguage
    ) = mockMvc.perform(
        patch("/api/v1/users/me/localization")
            .with(
                jwt()
                    .jwt { token ->
                        token
                            .subject(keycloakId.value.toString())
                            .claim("preferred_username", username)
                            .claim("telegram_id", telegramId.value)
                    }
                    .authorities(
                        SimpleGrantedAuthority("ROLE_USER")
                    )
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
            {
              "localizationLanguage": "${localizationLanguage.name}"
            }
            """.trimIndent()
            )
    )
}