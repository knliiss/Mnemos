package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.daily.DailyMenuContext
import com.mnemos.telegram.bot.application.daily.model.DailyMenuScreenData
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import org.springframework.stereotype.Component

@Component
class DailyMenuScreenDataProvider(
    private val gateway: DailyRewardGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.DAILY

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.Daily(
            DailyMenuScreenData(
                result = gateway.getState(session.telegramId),
                feedback = DailyMenuContext.feedback(session.screenContext),
            ),
        )
}
