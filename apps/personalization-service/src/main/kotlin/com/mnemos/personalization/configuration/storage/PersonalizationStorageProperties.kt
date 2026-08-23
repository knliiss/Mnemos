package com.mnemos.personalization.configuration.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.personalization.storage")
data class PersonalizationStorageProperties(
    val endpoint: String = "http://localhost:9000",
    val accessKey: String = "mnemos",
    val secretKey: String,
    val bucket: String = "mnemos-personalization",
) {
    init {
        require(endpoint.isNotBlank()) { "Personalization storage endpoint cannot be blank" }
        require(accessKey.isNotBlank()) { "Personalization storage access key cannot be blank" }
        require(secretKey.isNotBlank()) { "Personalization storage secret key cannot be blank" }
        require(BUCKET.matches(bucket)) { "Personalization storage bucket name is invalid" }
    }

    companion object {
        private val BUCKET = Regex("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$")
    }
}
