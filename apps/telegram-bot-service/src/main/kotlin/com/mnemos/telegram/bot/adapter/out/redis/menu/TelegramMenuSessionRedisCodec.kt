package com.mnemos.telegram.bot.adapter.out.redis.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

object TelegramMenuSessionRedisCodec {
    fun encode(session: TelegramMenuSession): String =
        listOf(
            FORMAT_VERSION,
            session.telegramId.toString(),
            session.menuMessageId?.toString().orEmpty(),
            session.currentScreen.name,
            session.navigationStack.joinToString(",") { screen -> screen.name },
            encodeContext(session.screenContext),
        ).joinToString("\n")

    fun decode(encoded: String): TelegramMenuSession? {
        val fields = encoded.split('\n')

        if (fields.size != FIELD_COUNT || fields[0] != FORMAT_VERSION) {
            return null
        }

        val telegramId = fields[1].toLongOrNull()?.takeIf { value -> value > 0 } ?: return null
        val menuMessageId = decodeMessageId(fields[2]) ?: return null
        val currentScreen = decodeScreen(fields[3]) ?: return null
        val navigationStack = decodeNavigationStack(fields[4]) ?: return null
        val screenContext = decodeContext(fields[5]) ?: return null

        return runCatching {
            TelegramMenuSession(
                telegramId = telegramId,
                menuMessageId = menuMessageId.value,
                currentScreen = currentScreen,
                navigationStack = navigationStack,
                screenContext = screenContext,
            )
        }.getOrNull()
    }

    private fun decodeMessageId(encoded: String): DecodedMessageId? {
        if (encoded.isBlank()) {
            return DecodedMessageId(null)
        }

        val messageId = encoded.toIntOrNull()?.takeIf { value -> value > 0 } ?: return null

        return DecodedMessageId(messageId)
    }

    private fun decodeScreen(encoded: String): TelegramMenuScreen? =
        runCatching {
            TelegramMenuScreen.valueOf(encoded)
        }.getOrNull()

    private fun decodeNavigationStack(encoded: String): List<TelegramMenuScreen>? {
        if (encoded.isBlank()) {
            return emptyList()
        }

        val screens = encoded.split(',').map(::decodeScreen)

        if (screens.any { screen -> screen == null }) {
            return null
        }

        return screens.filterNotNull()
    }

    private fun encodeContext(context: Map<String, String>): String =
        context
            .toSortedMap()
            .entries
            .joinToString(",") { (key, value) ->
                "${encodeToken(key)}:${encodeToken(value)}"
            }

    private fun decodeContext(encoded: String): Map<String, String>? {
        if (encoded.isBlank()) {
            return emptyMap()
        }

        val context = linkedMapOf<String, String>()

        for (entry in encoded.split(',')) {
            val parts = entry.split(':', limit = 2)

            if (parts.size != 2) {
                return null
            }

            val key = decodeToken(parts[0]) ?: return null
            val value = decodeToken(parts[1]) ?: return null

            context[key] = value
        }

        return context
    }

    private fun encodeToken(value: String): String = encoder.encodeToString(value.toByteArray(UTF_8))

    private fun decodeToken(value: String): String? = runCatching { String(decoder.decode(value), UTF_8) }.getOrNull()

    private data class DecodedMessageId(
        val value: Int?,
    )

    private const val FORMAT_VERSION = "1"
    private const val FIELD_COUNT = 6

    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
}
