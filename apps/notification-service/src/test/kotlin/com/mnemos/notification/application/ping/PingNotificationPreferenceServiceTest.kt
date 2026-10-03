package com.mnemos.notification.application.ping

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.ping.port.out.PingNotificationPreferenceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class PingNotificationPreferenceServiceTest {
    private val repository = mock<PingNotificationPreferenceRepository>()
    private val service = PingNotificationPreferenceService(repository)

    @Test
    fun `preferences are enabled by default when no row exists`() {
        val userId = UserId(UUID.randomUUID())

        whenever(repository.findEnabled(userId)).thenReturn(null)

        assertTrue(service.isEnabled(userId))
    }

    @Test
    fun `stored disabled preference overrides default`() {
        val userId = UserId(UUID.randomUUID())

        whenever(repository.findEnabled(userId)).thenReturn(false)

        assertFalse(service.isEnabled(userId))
    }

    @Test
    fun `bulk filtering defaults missing users to enabled`() {
        val enabledByDefault = UserId(UUID.randomUUID())
        val explicitlyDisabled = UserId(UUID.randomUUID())
        val explicitlyEnabled = UserId(UUID.randomUUID())
        val users = linkedSetOf(enabledByDefault, explicitlyDisabled, explicitlyEnabled)

        whenever(repository.findEnabled(users)).thenReturn(
            mapOf(
                explicitlyDisabled to false,
                explicitlyEnabled to true,
            ),
        )

        assertEquals(
            linkedSetOf(enabledByDefault, explicitlyEnabled),
            service.filterEnabled(users),
        )
    }

    @Test
    fun `update persists explicit user preference`() {
        val userId = UserId(UUID.randomUUID())

        service.update(userId, enabled = false)

        verify(repository).save(userId, false)
    }
}
