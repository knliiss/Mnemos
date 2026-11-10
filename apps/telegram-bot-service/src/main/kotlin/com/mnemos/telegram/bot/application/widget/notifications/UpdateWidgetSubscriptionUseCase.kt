package com.mnemos.telegram.bot.application.widget.notifications

interface UpdateWidgetSubscriptionUseCase {
    fun set(
        telegramId: Long,
        kind: WidgetSubscriptionKind,
        value: String,
        enabled: Boolean,
    ): WidgetSubscriptionUpdateResult

    fun setRaidLocations(telegramId: Long, locations: Set<Int>): WidgetSubscriptionUpdateResult
}
