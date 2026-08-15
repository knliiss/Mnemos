package com.mnemos.realtime.application.collector.security

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.HexFormat

@Component
class CollectorSecretHasher {
    fun hash(secret: String): String {
        val digest = MessageDigest.getInstance(ALGORITHM)
        val bytes = digest.digest(secret.toByteArray(StandardCharsets.UTF_8))

        return HexFormat.of().formatHex(bytes)
    }

    fun matches(
        secret: String,
        expectedHash: String,
    ): Boolean {
        val actual = hash(secret).toByteArray(StandardCharsets.US_ASCII)
        val expected = expectedHash.toByteArray(StandardCharsets.US_ASCII)

        return MessageDigest.isEqual(actual, expected)
    }

    private companion object {
        const val ALGORITHM = "SHA-256"
    }
}
