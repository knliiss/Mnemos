package com.mnemos.realtime.application.collector.security

import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant

class AuthenticateCollectorServiceTest {
    private val repository = mock<CollectorCredentialRepository>()
    private val secretHasher = CollectorSecretHasher()
    private val service =
        AuthenticateCollectorService(
            repository = repository,
            secretHasher = secretHasher,
        )

    @Test
    fun `authenticates an enabled collector with the correct secret`() {
        val secret = "collector-secret"
        val credential = createCredential(secret)
        val accessKey =
            CollectorAccessKey(
                credentialId = credential.id,
                secret = secret,
            )

        whenever(repository.findById(credential.id)).thenReturn(credential)

        val collectorId = service.authenticate(accessKey)

        assertEquals(credential.id, collectorId)
    }

    @Test
    fun `rejects disabled revoked and invalid collector credentials`() {
        val now = Instant.parse("2026-08-15T10:00:00Z")
        val enabled = createCredential("enabled-secret")
        val disabled = createCredential("disabled-secret").also { it.disable(now) }
        val revoked = createCredential("revoked-secret").also { it.revoke(now) }

        whenever(repository.findById(enabled.id)).thenReturn(enabled)
        whenever(repository.findById(disabled.id)).thenReturn(disabled)
        whenever(repository.findById(revoked.id)).thenReturn(revoked)

        assertNull(
            service.authenticate(
                CollectorAccessKey(
                    credentialId = enabled.id,
                    secret = "wrong-secret",
                ),
            ),
        )
        assertNull(
            service.authenticate(
                CollectorAccessKey(
                    credentialId = disabled.id,
                    secret = "disabled-secret",
                ),
            ),
        )
        assertNull(
            service.authenticate(
                CollectorAccessKey(
                    credentialId = revoked.id,
                    secret = "revoked-secret",
                ),
            ),
        )
    }

    private fun createCredential(secret: String): CollectorCredential {
        val now = Instant.parse("2026-08-15T09:00:00Z")

        return CollectorCredential.create(
            name = "test-collector",
            secretHash = secretHasher.hash(secret),
            now = now,
        )
    }
}
