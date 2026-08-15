package com.mnemos.realtime.application.collector.security

import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CollectorAccessKeyParser {
    fun parse(value: String): CollectorAccessKey? {
        val separator = value.indexOf('.')

        if (separator <= 0 || separator == value.lastIndex) {
            return null
        }

        val id =
            try {
                UUID.fromString(value.substring(0, separator))
            } catch (_: IllegalArgumentException) {
                return null
            }

        val secret = value.substring(separator + 1)

        if (!SECRET_PATTERN.matches(secret)) {
            return null
        }

        return CollectorAccessKey(
            credentialId = CollectorCredentialId(id),
            secret = secret,
        )
    }

    private companion object {
        val SECRET_PATTERN = Regex("^[A-Za-z0-9_-]{43}$")
    }
}
