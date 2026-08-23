package com.mnemos.realtime.application.collector.activation

import com.mnemos.realtime.application.collector.activation.usecase.redeem.RedeemCollectorActivationCommand
import com.mnemos.realtime.application.collector.activation.usecase.redeem.RedeemCollectorActivationService
import com.mnemos.realtime.application.collector.security.CollectorSecretHasher
import com.mnemos.realtime.domain.collector.model.CollectorActivation
import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.repository.CollectorActivationRepository
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class RedeemCollectorActivationServiceTest {
    private val activationRepository = mock<CollectorActivationRepository>()
    private val credentialRepository = mock<CollectorCredentialRepository>()
    private val secretHasher = CollectorSecretHasher()
    private val now = Instant.parse("2026-08-23T18:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val service =
        RedeemCollectorActivationService(
            activationRepository = activationRepository,
            credentialRepository = credentialRepository,
            secretHasher = secretHasher,
            clock = clock,
        )

    @Test
    fun `redeems activation into a user-owned collector credential`() {
        val token = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO12"
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000101")
        val secretHash = secretHasher.hash("installation-secret")
        val activation = activation(token, userId)

        whenever(
            activationRepository.findByTokenHashForUpdate(secretHasher.hash(token)),
        ).thenReturn(activation)
        whenever(credentialRepository.create(any())).thenAnswer { invocation -> invocation.arguments[0] }
        whenever(activationRepository.save(any())).thenAnswer { invocation -> invocation.arguments[0] }

        val result =
            service.execute(
                RedeemCollectorActivationCommand(
                    token = token,
                    deviceName = "Игровой компьютер",
                    secretHash = secretHash,
                ),
            )

        val credentialCaptor = argumentCaptor<CollectorCredential>()
        verify(credentialRepository).create(credentialCaptor.capture())

        val credential = credentialCaptor.firstValue

        assertEquals(userId, credential.ownerUserId)
        assertEquals(secretHash, credential.secretHash)
        assertEquals(credential.id, result.credentialId)
        assertEquals(credential.id, activation.credentialId)
    }

    @Test
    fun `returns the same credential when a successful redemption is retried`() {
        val token = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO12"
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000102")
        val secretHash = secretHasher.hash("installation-secret")
        val activation = activation(token, userId)
        val credential =
            CollectorCredential.create(
                name = "Игровой компьютер-existing",
                secretHash = secretHash,
                now = now,
                ownerUserId = userId,
            )

        activation.redeem(
            credentialId = credential.id,
            now = now,
        )

        whenever(
            activationRepository.findByTokenHashForUpdate(secretHasher.hash(token)),
        ).thenReturn(activation)
        whenever(credentialRepository.findById(credential.id)).thenReturn(credential)

        val result =
            service.execute(
                RedeemCollectorActivationCommand(
                    token = token,
                    deviceName = "Игровой компьютер",
                    secretHash = secretHash,
                ),
            )

        assertEquals(credential.id, result.credentialId)
        verify(credentialRepository, never()).create(any())
    }

    @Test
    fun `rejects a retry from another installation`() {
        val token = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO12"
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000103")
        val activation = activation(token, userId)
        val credential =
            CollectorCredential.create(
                name = "Игровой компьютер-existing",
                secretHash = secretHasher.hash("first-installation-secret"),
                now = now,
                ownerUserId = userId,
            )

        activation.redeem(
            credentialId = credential.id,
            now = now,
        )

        whenever(
            activationRepository.findByTokenHashForUpdate(secretHasher.hash(token)),
        ).thenReturn(activation)
        whenever(credentialRepository.findById(credential.id)).thenReturn(credential)

        assertThrows(CollectorActivationConflictException::class.java) {
            service.execute(
                RedeemCollectorActivationCommand(
                    token = token,
                    deviceName = "Другой компьютер",
                    secretHash = secretHasher.hash("second-installation-secret"),
                ),
            )
        }
    }

    @Test
    fun `rejects an expired activation before creating a credential`() {
        val token = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO12"
        val userId = UUID.fromString("019c1129-ef54-7000-8000-000000000104")
        val activation =
            CollectorActivation.create(
                userId = userId,
                tokenHash = secretHasher.hash(token),
                now = now.minus(Duration.ofMinutes(11)),
                ttl = Duration.ofMinutes(10),
            )

        whenever(
            activationRepository.findByTokenHashForUpdate(secretHasher.hash(token)),
        ).thenReturn(activation)

        assertThrows(CollectorActivationExpiredException::class.java) {
            service.execute(
                RedeemCollectorActivationCommand(
                    token = token,
                    deviceName = "Игровой компьютер",
                    secretHash = secretHasher.hash("installation-secret"),
                ),
            )
        }

        verify(credentialRepository, never()).create(any())
    }

    private fun activation(
        token: String,
        userId: UUID,
    ): CollectorActivation =
        CollectorActivation.create(
            userId = userId,
            tokenHash = secretHasher.hash(token),
            now = now,
            ttl = Duration.ofMinutes(10),
        )
}
