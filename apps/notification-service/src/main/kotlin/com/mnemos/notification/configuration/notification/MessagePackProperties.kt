package com.mnemos.notification.configuration.notification

import org.springframework.boot.context.properties.ConfigurationProperties

enum class MessagePackMode {
    LOCAL,
    REMOTE,
}

@ConfigurationProperties("mnemos.notification.message-pack")
data class MessagePackProperties(
    val mode: MessagePackMode = MessagePackMode.LOCAL,
) {
    init {
        require(mode == MessagePackMode.LOCAL) { "Remote message pack is not implemented. Change mode to local" }
    }
}
