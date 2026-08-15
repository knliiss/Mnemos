package com.mnemos.messaging.kafka.security

object KafkaMessageAuthenticationHeaders {
    const val SIGNATURE_VERSION = "mnemos-signature-version"
    const val SIGNATURE = "mnemos-signature"
    const val ED25519_V1 = "ed25519-v1"
}
