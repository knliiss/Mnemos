package com.mnemos.notification.application.ping.port.out

fun interface PingDeduplicationStore {
    fun accept(
        sender: String,
        message: String,
    ): Boolean

    fun release(
        sender: String,
        message: String,
    )
}
