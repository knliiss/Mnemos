package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentActorAccountGateway
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileCommentActorAccountGatewayAdapter(
    private val client: AccountServiceClient,
) : ProfileCommentActorAccountGateway {
    override fun activeAccountId(telegramId: Long): UUID? =
        client.getAccounts(telegramId)?.activeAccountId
}
