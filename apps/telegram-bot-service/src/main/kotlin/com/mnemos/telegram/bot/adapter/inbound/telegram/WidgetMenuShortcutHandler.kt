package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profiles.OpenProfileWidgetUseCase
import com.mnemos.telegram.bot.application.widget.events.OpenEventWidgetUseCase
import com.mnemos.telegram.bot.application.widget.management.OpenAccountManagementWidgetUseCase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class WidgetMenuShortcutHandler(
    private val menuService: TelegramMenuService,
    private val profiles: OpenProfileWidgetUseCase? = null,
    private val eventWidgets: OpenEventWidgetUseCase? = null,
    private val management: OpenAccountManagementWidgetUseCase? = null,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = "w:open:"

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (context.data == "w:open:account-manage") {
            val widget = management?.takeIf { it.enabled }
            if (widget != null) {
                when (val result = widget.execute(context.telegramId)) {
                    is TelegramDeliveryResult.PermanentFailure -> Unit
                    else -> return result
                }
            }
            return menuService.open(context.telegramId, TelegramMenuScreen.ACCOUNT_MANAGEMENT)
        }
        if (context.data == "w:open:events") {
            val widget = eventWidgets?.takeIf { it.enabled }
            if (widget != null) {
                when (val result = widget.execute(context.telegramId)) {
                    is TelegramDeliveryResult.PermanentFailure -> Unit
                    else -> return result
                }
            }
            return menuService.open(context.telegramId, TelegramMenuScreen.EVENTS)
        }

        if (context.data == "w:open:profiles") {
            val widget = profiles?.takeIf { it.enabled }
            if (widget != null) {
                when (val result = widget.execute(context.telegramId)) {
                    is TelegramDeliveryResult.PermanentFailure -> Unit
                    else -> return result
                }
            }
            return menuService.open(context.telegramId, TelegramMenuScreen.HOME)
        }
        val screen =
            when (context.data) {
                "w:open:events" -> TelegramMenuScreen.EVENTS
                "w:open:notifications" -> TelegramMenuScreen.NOTIFICATIONS
                "w:open:account" -> TelegramMenuScreen.ACCOUNT
                "w:open:daily" -> TelegramMenuScreen.DAILY
                "w:open:bets" -> TelegramMenuScreen.BETS
                "w:open:prime" -> TelegramMenuScreen.PRIME
                "w:open:online" -> TelegramMenuScreen.ONLINE
                "w:open:settings" -> TelegramMenuScreen.SETTINGS
                "w:open:account-invites" -> TelegramMenuScreen.ACCOUNT_INVITES
                else -> return TelegramDeliveryResult.Delivered
            }

        return menuService.open(context.telegramId, screen)
    }
}
