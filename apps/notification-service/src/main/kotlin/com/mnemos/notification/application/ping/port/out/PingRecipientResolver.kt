package com.mnemos.notification.application.ping.port.out

import com.mnemos.common.core.model.id.UserId

fun interface PingRecipientResolver {
    fun resolve(nickname: String): Set<UserId>
}
