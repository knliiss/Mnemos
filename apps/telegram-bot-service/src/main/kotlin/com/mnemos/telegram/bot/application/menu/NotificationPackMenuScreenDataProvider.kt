package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.pack.NotificationPackMenuContext
import com.mnemos.telegram.bot.application.pack.model.NotificationPackDetailResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.pack.port.out.NotificationPackGateway
import org.springframework.stereotype.Component

@Component
class NotificationPackMenuScreenDataProvider(
    private val gateway: NotificationPackGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.PACKS

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        val mode = NotificationPackMenuContext.mode(session.screenContext)
        val selectedPersonalizationId = NotificationPackMenuContext.selectedPersonalizationId(session.screenContext)
        val store =
            if (mode == NotificationPackMenuMode.STORE) {
                gateway.store(
                    telegramId = session.telegramId,
                    page = NotificationPackMenuContext.page(session.screenContext),
                    size = STORE_PAGE_SIZE,
                )
            } else {
                null
            }
        val detail =
            if (mode == NotificationPackMenuMode.DETAIL) {
                selectedPersonalizationId
                    ?.let { personalizationId ->
                        gateway.detail(
                            telegramId = session.telegramId,
                            personalizationId = personalizationId,
                        )
                    }
                    ?: NotificationPackDetailResult.NotFound
            } else {
                null
            }
        val owned =
            if (mode == NotificationPackMenuMode.OWNED) {
                gateway.owned(session.telegramId)
            } else {
                null
            }

        return TelegramMenuScreenData.Packs(
            NotificationPackMenuScreenData(
                mode = mode,
                store = store,
                detail = detail,
                owned = owned,
                selectedPersonalizationId = selectedPersonalizationId,
                selectedTemplateType = NotificationPackMenuContext.selectedTemplateType(session.screenContext),
                selectedVariantId = NotificationPackMenuContext.selectedVariantId(session.screenContext),
                feedback = NotificationPackMenuContext.feedback(session.screenContext),
            ),
        )
    }

    private companion object {
        const val STORE_PAGE_SIZE = 6
    }
}
