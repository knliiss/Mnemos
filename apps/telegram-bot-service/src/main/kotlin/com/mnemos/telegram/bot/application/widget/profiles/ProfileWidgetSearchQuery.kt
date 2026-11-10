package com.mnemos.telegram.bot.application.widget.profiles

object ProfileWidgetSearchQuery {
    fun normalize(value: String): String? {
        val query = value.trim()
        return query.takeIf { it.length in 2..64 && it.toByteArray(Charsets.UTF_8).size <= 256 }
    }
}
