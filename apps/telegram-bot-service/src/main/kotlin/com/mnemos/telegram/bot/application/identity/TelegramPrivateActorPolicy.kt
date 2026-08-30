package com.mnemos.telegram.bot.application.identity

object TelegramPrivateActorPolicy {
    fun resolve(
        isPrivateChat: Boolean,
        chatId: Long,
        actorId: Long?,
    ): Long? {
        if (!isPrivateChat) {
            return null
        }

        return actorId?.takeIf { id -> id == chatId }
    }
}
