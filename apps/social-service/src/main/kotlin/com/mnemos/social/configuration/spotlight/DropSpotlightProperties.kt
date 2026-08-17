package com.mnemos.social.configuration.spotlight

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.social.spotlight")
data class DropSpotlightProperties(
    val pricePrime: Long,
    val dropMaxAge: Duration,
    val lifetime: Duration,
    val buyerCooldown: Duration,
    val globalGap: Duration,
    val recipientGap: Duration,
    val maxQueueDelay: Duration,
)
