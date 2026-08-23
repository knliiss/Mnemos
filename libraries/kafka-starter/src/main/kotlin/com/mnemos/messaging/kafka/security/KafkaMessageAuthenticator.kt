package com.mnemos.messaging.kafka.security

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class KafkaMessageAuthenticator(
    private val serviceName: String,
    private val privateKey: PrivateKey,
    private val trustedPublicKeys: Map<String, PublicKey>,
) {
    fun sign(
        producer: String,
        topic: String,
        key: String?,
        payload: String,
    ): String {
        require(producer == serviceName) {
            "Kafka message producer $producer does not match local service $serviceName"
        }
        require(isProducerAllowedForTopic(producer, topic)) {
            "Kafka producer $producer is not allowed to publish to $topic"
        }

        val signer = Signature.getInstance(ALGORITHM)
        signer.initSign(privateKey)
        signer.update(canonicalMessage(producer, topic, key, payload))

        return Base64.getEncoder().encodeToString(signer.sign())
    }

    fun verify(
        producer: String,
        topic: String,
        key: String?,
        payload: String,
        encodedSignature: String,
    ): Boolean {
        if (!isProducerAllowedForTopic(producer, topic)) {
            return false
        }

        val publicKey = trustedPublicKeys[producer] ?: return false
        val signature =
            try {
                Base64.getDecoder().decode(encodedSignature)
            } catch (_: IllegalArgumentException) {
                return false
            }

        val verifier = Signature.getInstance(ALGORITHM)
        verifier.initVerify(publicKey)
        verifier.update(canonicalMessage(producer, topic, key, payload))

        return verifier.verify(signature)
    }

    companion object {
        private const val ALGORITHM = "Ed25519"
        private const val LOCAL_KEY_CHECK_MESSAGE = "mnemos-kafka-message-auth-local-key-check-v1"

        fun fromEncodedKeys(
            serviceName: String,
            encodedPrivateKey: String,
            encodedTrustedPublicKeys: Map<String, String>,
        ): KafkaMessageAuthenticator {
            require(SERVICE_PATTERN.matches(serviceName)) {
                "Invalid Kafka signing service name: $serviceName"
            }
            require(encodedPrivateKey.isNotBlank()) {
                "Kafka message authentication private key must not be blank"
            }
            require(encodedTrustedPublicKeys.isNotEmpty()) {
                "Kafka message authentication trusted public keys must not be empty"
            }

            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            val privateKey =
                keyFactory.generatePrivate(
                    PKCS8EncodedKeySpec(
                        Base64.getDecoder().decode(encodedPrivateKey),
                    ),
                )
            val publicKeys =
                encodedTrustedPublicKeys.mapValues { (_, encodedPublicKey) ->
                    keyFactory.generatePublic(
                        X509EncodedKeySpec(
                            Base64.getDecoder().decode(encodedPublicKey),
                        ),
                    )
                }
            val localPublicKey =
                requireNotNull(publicKeys[serviceName]) {
                    "Kafka trusted public keys must include local service $serviceName"
                }

            require(keysMatch(privateKey, localPublicKey)) {
                "Kafka signing private key does not match trusted public key for local service $serviceName"
            }

            return KafkaMessageAuthenticator(
                serviceName = serviceName,
                privateKey = privateKey,
                trustedPublicKeys = publicKeys,
            )
        }

        private fun keysMatch(
            privateKey: PrivateKey,
            publicKey: PublicKey,
        ): Boolean {
            val challenge = LOCAL_KEY_CHECK_MESSAGE.toByteArray(StandardCharsets.UTF_8)
            val signer = Signature.getInstance(ALGORITHM)

            signer.initSign(privateKey)
            signer.update(challenge)

            val signature = signer.sign()
            val verifier = Signature.getInstance(ALGORITHM)

            verifier.initVerify(publicKey)
            verifier.update(challenge)

            return verifier.verify(signature)
        }

        private fun isProducerAllowedForTopic(
            producer: String,
            topic: String,
        ): Boolean {
            val eventOwner =
                EVENT_TOPIC_PATTERN
                    .matchEntire(topic)
                    ?.groupValues
                    ?.get(1)
                    ?.let { context -> "$context-service" }

            return eventOwner == null || eventOwner == producer
        }

        private fun canonicalMessage(
            producer: String,
            topic: String,
            key: String?,
            payload: String,
        ): ByteArray {
            val output = ByteArrayOutputStream()

            DataOutputStream(output).use { data ->
                data.writeField(producer)
                data.writeField(topic)
                data.writeField(key.orEmpty())
                data.writeField(payload)
            }

            return output.toByteArray()
        }

        private fun DataOutputStream.writeField(value: String) {
            val bytes = value.toByteArray(StandardCharsets.UTF_8)
            writeInt(bytes.size)
            write(bytes)
        }

        private val SERVICE_PATTERN = Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")
        private val EVENT_TOPIC_PATTERN = Regex("^mnemos\\.([a-z][a-z0-9-]*)\\.events\\.v[1-9][0-9]*$")
    }
}
