package com.mnemos.notification.adapter.out.account

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.ping.port.out.PingRecipientResolver
import org.springframework.stereotype.Component

@Component
class AccountPingRecipientResolver(
    private val client: NotificationAccountServiceClient,
) : PingRecipientResolver {
    override fun resolve(nickname: String): Set<UserId> =
        client
            .findActiveAccountUsers(nickname)
            .users
            .mapTo(linkedSetOf()) { user -> UserId(user.userId) }
}
