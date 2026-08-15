package com.mnemos.realtime.application.collector.security

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class CollectorSecretGenerator(
    private val secureRandom: SecureRandom,
) {
    fun generate(): String {
        val bytes = ByteArray(SECRET_BYTES)
        secureRandom.nextBytes(bytes)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private companion object {
        const val SECRET_BYTES = 32
    }
}
