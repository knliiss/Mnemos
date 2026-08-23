package com.mnemos.messaging.kafka.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Base64

class KafkaMessageAuthenticatorTest {
    @Test
    fun `verifies message signed by trusted producer`() {
        val keyPair = generateKeyPair()
        val authenticator = authenticator("bet-service", keyPair)
        val signature =
            authenticator.sign(
                producer = "bet-service",
                topic = "mnemos.bet.events.v1",
                key = "operation-1",
                payload = "{\"amount\":100}",
            )

        assertTrue(
            authenticator.verify(
                producer = "bet-service",
                topic = "mnemos.bet.events.v1",
                key = "operation-1",
                payload = "{\"amount\":100}",
                encodedSignature = signature,
            ),
        )
    }

    @Test
    fun `shared trusted key set verifies another service command`() {
        val rewardKeyPair = generateKeyPair()
        val walletKeyPair = generateKeyPair()
        val trustedPublicKeys =
            mapOf(
                "reward-service" to encodedPublicKey(rewardKeyPair),
                "wallet-service" to encodedPublicKey(walletKeyPair),
            )
        val rewardAuthenticator =
            authenticator(
                serviceName = "reward-service",
                keyPair = rewardKeyPair,
                trustedPublicKeys = trustedPublicKeys,
            )
        val walletAuthenticator =
            authenticator(
                serviceName = "wallet-service",
                keyPair = walletKeyPair,
                trustedPublicKeys = trustedPublicKeys,
            )
        val signature =
            rewardAuthenticator.sign(
                producer = "reward-service",
                topic = "mnemos.wallet.commands.v2",
                key = "request-1",
                payload = "{\"amount\":60}",
            )

        assertTrue(
            walletAuthenticator.verify(
                producer = "reward-service",
                topic = "mnemos.wallet.commands.v2",
                key = "request-1",
                payload = "{\"amount\":60}",
                encodedSignature = signature,
            ),
        )
    }

    @Test
    fun `rejects local private key that does not match trusted public key`() {
        val localKeyPair = generateKeyPair()
        val differentKeyPair = generateKeyPair()

        assertThrows(IllegalArgumentException::class.java) {
            KafkaMessageAuthenticator.fromEncodedKeys(
                serviceName = "wallet-service",
                encodedPrivateKey = encodedPrivateKey(localKeyPair),
                encodedTrustedPublicKeys =
                    mapOf(
                        "wallet-service" to encodedPublicKey(differentKeyPair),
                    ),
            )
        }
    }

    @Test
    fun `rejects trusted key set without local service`() {
        val localKeyPair = generateKeyPair()
        val differentKeyPair = generateKeyPair()

        assertThrows(IllegalArgumentException::class.java) {
            KafkaMessageAuthenticator.fromEncodedKeys(
                serviceName = "wallet-service",
                encodedPrivateKey = encodedPrivateKey(localKeyPair),
                encodedTrustedPublicKeys =
                    mapOf(
                        "reward-service" to encodedPublicKey(differentKeyPair),
                    ),
            )
        }
    }

    @Test
    fun `rejects payload tampering`() {
        val keyPair = generateKeyPair()
        val authenticator = authenticator("bet-service", keyPair)
        val signature =
            authenticator.sign(
                producer = "bet-service",
                topic = "mnemos.bet.events.v1",
                key = "operation-1",
                payload = "{\"amount\":100}",
            )

        assertFalse(
            authenticator.verify(
                producer = "bet-service",
                topic = "mnemos.bet.events.v1",
                key = "operation-1",
                payload = "{\"amount\":1000000}",
                encodedSignature = signature,
            ),
        )
    }

    @Test
    fun `local service cannot sign as another producer`() {
        val authenticator = authenticator("bet-service", generateKeyPair())

        assertThrows(IllegalArgumentException::class.java) {
            authenticator.sign(
                producer = "wallet-service",
                topic = "mnemos.wallet.events.v2",
                key = "operation-1",
                payload = "{}",
            )
        }
    }

    @Test
    fun `service cannot publish another contexts event topic`() {
        val authenticator = authenticator("bet-service", generateKeyPair())

        assertThrows(IllegalArgumentException::class.java) {
            authenticator.sign(
                producer = "bet-service",
                topic = "mnemos.wallet.events.v2",
                key = "operation-1",
                payload = "{}",
            )
        }
    }

    @Test
    fun `trusted signature is rejected on another contexts event topic`() {
        val keyPair = generateKeyPair()
        val authenticator = authenticator("bet-service", keyPair)
        val signature =
            authenticator.sign(
                producer = "bet-service",
                topic = "mnemos.wallet.commands.v2",
                key = "operation-1",
                payload = "{}",
            )

        assertFalse(
            authenticator.verify(
                producer = "bet-service",
                topic = "mnemos.wallet.events.v2",
                key = "operation-1",
                payload = "{}",
                encodedSignature = signature,
            ),
        )
    }

    @Test
    fun `rejects signature from different key`() {
        val trustedKeyPair = generateKeyPair()
        val attackerKeyPair = generateKeyPair()
        val trustedAuthenticator = authenticator("bet-service", trustedKeyPair)
        val attackerAuthenticator = authenticator("bet-service", attackerKeyPair)
        val signature =
            attackerAuthenticator.sign(
                producer = "bet-service",
                topic = "mnemos.bet.events.v1",
                key = "operation-1",
                payload = "{}",
            )

        assertFalse(
            trustedAuthenticator.verify(
                producer = "bet-service",
                topic = "mnemos.bet.events.v1",
                key = "operation-1",
                payload = "{}",
                encodedSignature = signature,
            ),
        )
    }

    private fun authenticator(
        serviceName: String,
        keyPair: KeyPair,
        trustedPublicKeys: Map<String, String> =
            mapOf(
                serviceName to encodedPublicKey(keyPair),
            ),
    ): KafkaMessageAuthenticator =
        KafkaMessageAuthenticator.fromEncodedKeys(
            serviceName = serviceName,
            encodedPrivateKey = encodedPrivateKey(keyPair),
            encodedTrustedPublicKeys = trustedPublicKeys,
        )

    private fun encodedPrivateKey(keyPair: KeyPair): String =
        Base64.getEncoder().encodeToString(keyPair.private.encoded)

    private fun encodedPublicKey(keyPair: KeyPair): String =
        Base64.getEncoder().encodeToString(keyPair.public.encoded)

    private fun generateKeyPair(): KeyPair =
        KeyPairGenerator
            .getInstance("Ed25519")
            .generateKeyPair()
}
