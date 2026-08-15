package com.mnemos.security

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "mnemos.security")
data class MnemosSecurityProperties(
    val publicPaths: List<String> = listOf(
        "/actuator/health",
        "/actuator/health/**"
    ),

    val rolePrefix: String = "ROLE_",
    val principalClaim: String = "preferred_username",
    val internal: InternalSecurityProperties = InternalSecurityProperties()
)

data class InternalSecurityProperties(
    val serviceHeader: String = "X-Mnemos-Service",
    val apiKeyHeader: String = "X-Mnemos-API-Key",
    val apiKeys: Map<String, String> = emptyMap(),
)
