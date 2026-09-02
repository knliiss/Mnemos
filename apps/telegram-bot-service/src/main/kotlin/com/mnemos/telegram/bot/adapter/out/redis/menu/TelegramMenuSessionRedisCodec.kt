package com.mnemos.telegram.bot.adapter.out.redis.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

object TelegramMenuSessionRedisCodec {
    fun encode(session: TelegramMenuSession): String =
        listOf(
            CURRENT_FORMAT_VERSION,
            session.telegramId.toString(),
            session.menuMessageId?.toString().orEmpty(),
            session.currentScreen.name,
            session.navigationStack.joinToString(",") { screen -> screen.name },
            session.pendingInput?.name.orEmpty(),
            encodeContext(session.screenContext),
        ).joinToString("\n")

    fun decode(encoded: String): TelegramMenuSession? {
        val fields = encoded.split('\n')

        return when (fields.firstOrNull()) {
            FORMAT_VERSION_1 -> decodeVersion1(fields)
            CURRENT_FORMAT_VERSION -> decodeVersion2(fields)
            else -> null
        }
    }

    private fun decodeVersion1(fields: List<String>): TelegramMenuSession? {
        if (fields.size != VERSION_1_FIELD_COUNT) {
            return null
        }

        return decodeSession(
            telegramIdField = fields[1],
            messageIdField = fields[2],
            screenField = fields[3],
            navigationField = fields[4],
            pendingInputField = "",
            contextField = fields[5],
        )
    }

    private fun decodeVersion2(fields: List<String>): TelegramMenuSession? {
        if (fields.size != VERSION_2_FIELD_COUNT) {
            return null
        }

        return decodeSession(
            telegramIdField = fields[1],
            messageIdField = fields[2],
            screenField = fields[3],
            navigationField = fields[4],
            pendingInputField = fields[5],
            contextField = fields[6],
        )
    }

    private fun decodeSession(
        telegramIdField: String,
        messageIdField: String,
        screenField: String,
        navigationField: String,
        pendingInputField: String,
        contextField: String,
    ): TelegramMenuSession? {
        val telegramId = telegramIdField.toLongOrNull()?.takeIf { value -> value > 0 } ?: return null
        val menuMessageId = decodeMessageId(messageIdField) ?: return null
        val currentScreen = decodeScreen(screenField) ?: return null
        val navigationStack = decodeNavigationStack(navigationField) ?: return null
        val pendingInput = decodePendingInput(pendingInputField) ?: return null
        val screenContext = decodeContext(contextField) ?: return null

        return runCatching {
            TelegramMenuSession(
                telegramId = telegramId,
                menuMessageId = menuMessageId.value,
                currentScreen = currentScreen,
                navigationStack = navigationStack,
                pendingInput = pendingInput.value,
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

    private fun decodePendingInput(encoded: String): DecodedPendingInput? {
        if (encoded.isBlank()) {
            return DecodedPendingInput(null)
        }

        val pendingInput =
            runCatching {
                TelegramPendingInput.valueOf(encoded)
            }.getOrNull() ?: return null

        return DecodedPendingInput(pendingInput)
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

    private data class DecodedPendingInput(
        val value: TelegramPendingInput?,
    )

    private const val FORMAT_VERSION_1 = "1"
    private const val CURRENT_FORMAT_VERSION = "2"
    private const val VERSION_1_FIELD_COUNT = 6
    private const val VERSION_2_FIELD_COUNT = 7

    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
}
