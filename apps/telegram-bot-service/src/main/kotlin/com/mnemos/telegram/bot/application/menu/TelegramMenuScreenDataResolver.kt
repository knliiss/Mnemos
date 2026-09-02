package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import org.springframework.stereotype.Component

@Component
class TelegramMenuScreenDataResolver(
    providers: List<TelegramMenuScreenDataProvider>,
) {
    private val providersByScreen = providers.associateBy { provider -> provider.screen }

    init {
        require(providersByScreen.size == providers.size) {
            "Only one Telegram menu data provider can be registered for a screen"
        }
    }

    fun resolve(session: TelegramMenuSession): TelegramMenuScreenData =
        providersByScreen[session.currentScreen]?.load(session) ?: TelegramMenuScreenData.Empty
}
