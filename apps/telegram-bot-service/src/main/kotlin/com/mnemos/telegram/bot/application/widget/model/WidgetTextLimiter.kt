package com.mnemos.telegram.bot.application.widget.model

import java.nio.charset.StandardCharsets

object WidgetTextLimiter {
    fun truncateUtf8(
        value: String,
        maxBytes: Int,
    ): String {
        require(maxBytes >= 0)
        if (value.toByteArray(StandardCharsets.UTF_8).size <= maxBytes) return value

        val result = StringBuilder()
        var offset = 0
        var byteCount = 0
        while (offset < value.length) {
            val codePoint = Character.codePointAt(value, offset)
            val unit = String(Character.toChars(codePoint))
            val size = unit.toByteArray(StandardCharsets.UTF_8).size
            if (byteCount + size > maxBytes) break
            result.appendCodePoint(codePoint)
            offset += Character.charCount(codePoint)
            byteCount += size
        }
        return result.toString()
    }
}
