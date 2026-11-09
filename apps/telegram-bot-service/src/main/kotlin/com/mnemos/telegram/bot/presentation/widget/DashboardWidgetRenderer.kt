package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetButton
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import org.springframework.stereotype.Component

@Component
class DashboardWidgetRenderer {
    fun render(view: DashboardWidgetView): TelegramWidgetMessage {
        val accountName = escape(view.accountName ?: "Акаунт не вибрано")
        val coin = view.coin?.toString() ?: "—"
        val prime = view.prime?.toString() ?: "—"
        val streak = view.dailyStreak?.toString() ?: "—"
        val accountCount = view.accountCount
        val dailyStatus =
            when (view.dailyClaimed) {
                true -> "Отримано сьогодні"
                false -> "Нагорода доступна"
                null -> "Відкрийте меню нагород"
            }
        val accountDescription =
            if (view.accountName == null) {
                "Створіть акаунт або приєднайтеся до команди"
            } else {
                "Доступних акаунтів: $accountCount"
            }

        val html =
            """
            <style>
              body { margin: 0; padding: 14px; font-family: system-ui, sans-serif; }
              .head { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
              .brand { font-size: 17px; font-weight: 800; letter-spacing: 1px; }
              .tag { padding: 5px 9px; border-radius: 20px; background: var(--tg-theme-secondary-bg-color); font-size: 11px; }
              .account { margin-top: 16px; font-weight: 700; font-size: 17px; overflow-wrap: anywhere; }
              .hint { color: var(--tg-theme-hint-color); font-size: 12px; margin-top: 5px; }
              .stats { display: flex; gap: 8px; margin-top: 16px; }
              .stat { flex: 1; min-width: 0; border-radius: 12px; background: var(--tg-theme-secondary-bg-color); padding: 12px 9px; }
              .num { font-weight: 750; font-size: 20px; }
              .label { color: var(--tg-theme-hint-color); font-size: 11px; margin-top: 3px; }
              .daily { margin-top: 12px; padding: 12px; border: 1px solid var(--tg-theme-section-separator-color); border-radius: 12px; }
              .daily-title { font-weight: 700; font-size: 13px; }
            </style>
            <div class="head"><span class="brand">MNEMOS</span><span class="tag">SAO</span></div>
            <div class="account">$accountName</div>
            <div class="hint">$accountDescription</div>
            <div class="stats">
              <div class="stat"><div class="num">$coin</div><div class="label">COIN</div></div>
              <div class="stat"><div class="num">$prime</div><div class="label">PRIME</div></div>
            </div>
            <div class="daily">
              <div class="daily-title">Щоденна нагорода · серія $streak</div>
              <div class="hint">$dailyStatus</div>
            </div>
            """.trimIndent()

        return TelegramWidgetMessage(
            html = html,
            caption = "Mnemos · головна панель. Кнопки під карткою відкривають розділи бота.",
            actionRows =
                listOf(
                    listOf(
                        TelegramWidgetButton("Події", "w:open:events"),
                        TelegramWidgetButton("Підписки", "w:open:notifications"),
                    ),
                    listOf(
                        TelegramWidgetButton("Акаунт", "w:open:account"),
                        TelegramWidgetButton("Нагороди", "w:open:daily"),
                    ),
                ),
        )
    }

    private fun escape(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
