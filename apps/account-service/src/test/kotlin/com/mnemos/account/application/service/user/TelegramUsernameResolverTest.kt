package com.mnemos.account.application.service.user

import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TelegramUsernameResolverTest {
    private val userRepository = mock<UserRepository>()
    private val resolver = TelegramUsernameResolver(userRepository)

    @Test
    fun `Telegram username is the primary Mnemos username`() {
        val telegramId = TelegramId(910484582)
        val primary = Username("knalis")
        val collision = Username("knalis_910484582")

        whenever(userRepository.existsByUsername(primary)).thenReturn(false)
        whenever(userRepository.existsByUsername(collision)).thenReturn(false)

        val resolution = resolver.resolve(telegramId, "knalis")

        assertEquals(primary, resolution.selected)
        assertEquals(collision, resolution.keycloakFallback)
    }

    @Test
    fun `duplicate Telegram username falls back to username and Telegram id`() {
        val telegramId = TelegramId(910484582)
        val primary = Username("knalis")
        val collision = Username("knalis_910484582")

        whenever(userRepository.existsByUsername(primary)).thenReturn(true)
        whenever(userRepository.existsByUsername(collision)).thenReturn(false)

        val resolution = resolver.resolve(telegramId, "knalis")

        assertEquals(collision, resolution.selected)
        assertNull(resolution.keycloakFallback)
    }

    @Test
    fun `missing Telegram username falls back to Telegram id`() {
        val telegramId = TelegramId(910484582)
        val primary = Username("910484582")
        val collision = Username("910484582_910484582")

        whenever(userRepository.existsByUsername(primary)).thenReturn(false)
        whenever(userRepository.existsByUsername(collision)).thenReturn(false)

        val resolution = resolver.resolve(telegramId, null)

        assertEquals(primary, resolution.selected)
        assertEquals(collision, resolution.keycloakFallback)
    }

    @Test
    fun `leading Telegram at sign is not stored in Mnemos username`() {
        val telegramId = TelegramId(910484582)
        val primary = Username("knalis")
        val collision = Username("knalis_910484582")

        whenever(userRepository.existsByUsername(primary)).thenReturn(false)
        whenever(userRepository.existsByUsername(collision)).thenReturn(false)

        val resolution = resolver.resolve(telegramId, "@knalis")

        assertEquals(primary, resolution.selected)
    }
}
