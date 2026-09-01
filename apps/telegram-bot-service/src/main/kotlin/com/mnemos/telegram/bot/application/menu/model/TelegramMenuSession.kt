package com.mnemos.telegram.bot.application.menu.model

data class TelegramMenuSession(
    val telegramId: Long,
    val menuMessageId: Int? = null,
    val currentScreen: TelegramMenuScreen = TelegramMenuScreen.HOME,
    val navigationStack: List<TelegramMenuScreen> = emptyList(),
    val pendingInput: TelegramPendingInput? = null,
    val screenContext: Map<String, String> = emptyMap(),
) {
    init {
        require(telegramId > 0)
        require(menuMessageId == null || menuMessageId > 0)
        require(navigationStack.size <= MAX_NAVIGATION_DEPTH)
        require(pendingInput == null || pendingInput.screen == currentScreen)
    }

    private companion object {
        const val MAX_NAVIGATION_DEPTH = 16
    }
}