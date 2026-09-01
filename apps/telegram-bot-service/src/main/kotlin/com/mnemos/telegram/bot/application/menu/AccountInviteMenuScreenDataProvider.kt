package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.invite.AccountInviteMenuContext
import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuScreenData
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import org.springframework.stereotype.Component

@Component
class AccountInviteMenuScreenDataProvider(
    private val gateway: AccountInviteGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ACCOUNT_INVITES

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.AccountInvites(
            AccountInviteMenuScreenData(
                result = gateway.getInvites(session.telegramId),
                expiry = AccountInviteMenuContext.expiry(session.screenContext),
                feedback = AccountInviteMenuContext.feedback(session.screenContext),
                revokeConfirmationId = AccountInviteMenuContext.revokeConfirmationId(session.screenContext),
            ),
        )
}
