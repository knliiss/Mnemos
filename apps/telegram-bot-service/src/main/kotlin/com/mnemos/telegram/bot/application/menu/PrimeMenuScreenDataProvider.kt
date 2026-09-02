package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.prime.PrimeMenuContext
import com.mnemos.telegram.bot.application.prime.model.PrimeMenuScreenData
import com.mnemos.telegram.bot.application.prime.port.out.PrimePurchaseGateway
import org.springframework.stereotype.Component

@Component
class PrimeMenuScreenDataProvider(
    private val gateway: PrimePurchaseGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.PRIME

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.Prime(
            data =
                PrimeMenuScreenData(
                    packages = gateway.getCatalog(),
                    invoice = PrimeMenuContext.invoice(session.screenContext),
                    notice = PrimeMenuContext.notice(session.screenContext),
                ),
        )
}
