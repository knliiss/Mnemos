package com.mnemos.notification.adapter.out.message.local

object TelegramHtmlEscaper {
    fun escape(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
}
