package com.mnemos.telegram.bot.application.menu.model

data class TelegramMenuSession(
    val telegramId: Long,
    val menuMessageId: Int? = null,
    val currentScreen: TelegramMenuScreen = TelegramMenuScreen.HOME,
    val navigationStack: List<TelegramMenuScreen> = emptyList(),
    val screenContext: Map<String, String> = emptyMap(),
) {
    init {
        require(telegramId > 0)
        require(menuMessageId == null || menuMessageId > 0)
        require(navigationStack.size <= MAX_NAVIGATION_DEPTH)
    }

    private companion object {
        const val MAX_NAVIGATION_DEPTH = 16
    }
}
