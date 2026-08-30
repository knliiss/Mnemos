package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.AccountMenuContext
import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import org.springframework.stereotype.Component

@Component
class AccountMenuScreenDataProvider(
    private val gateway: AccountGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ACCOUNT

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.Account(
            AccountMenuScreenData(
                result = gateway.getContext(session.telegramId),
                feedback = AccountMenuContext.feedback(session.screenContext),
            ),
        )
}
