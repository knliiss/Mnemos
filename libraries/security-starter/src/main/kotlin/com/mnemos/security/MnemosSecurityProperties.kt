package com.mnemos.security

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "mnemos.security")
data class MnemosSecurityProperties(
    val publicPaths: List<String> = listOf(
        "/actuator/health",
        "/actuator/health/**"
    ),

    val rolePrefix: String = "ROLE_",

    val principalClaim: String = "preferred_username"
)