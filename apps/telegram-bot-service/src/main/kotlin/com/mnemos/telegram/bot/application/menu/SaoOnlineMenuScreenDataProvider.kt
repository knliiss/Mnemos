package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import org.springframework.stereotype.Component

@Component
class SaoOnlineMenuScreenDataProvider(
    private val snapshotGateway: SaoOnlineSnapshotGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ONLINE

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.SaoOnline(snapshotGateway.getSnapshot())
}
