package com.mnemos.bet.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.bet.item-race")
data class ItemRaceProperties(
    val registrationDuration: Duration = Duration.ofMinutes(10),
    val entryConfirmationGrace: Duration = Duration.ofSeconds(30),
)
