package com.mnemos.telegram.bot.application.widget.dashboard

data class DashboardWidgetView(
    val accountName: String?,
    val accountCount: Int,
    val coin: Long?,
    val prime: Long?,
    val dailyStreak: Int?,
    val dailyClaimed: Boolean?,
    val accountId: java.util.UUID? = null,
)
