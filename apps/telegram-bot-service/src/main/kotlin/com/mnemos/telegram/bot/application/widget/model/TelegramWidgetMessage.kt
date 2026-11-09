package com.mnemos.telegram.bot.application.widget.model

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

data class TelegramWidgetMessage(
    val html: String,
    val caption: String,
    val actionRows: List<List<TelegramWidgetButton>> = emptyList(),
    val chatData: String? = null,
    val allowQueries: Boolean = false,
) {
    init {
        val htmlBytes = html.toByteArray(StandardCharsets.UTF_8)
        require(htmlBytes.size in 1..MAX_HTML_BYTES)
        require(compressedSize(htmlBytes) <= MAX_COMPRESSED_HTML_BYTES)
        require(caption.isNotBlank() && caption.length <= MAX_CAPTION_LENGTH)
        require(actionRows.none(List<TelegramWidgetButton>::isEmpty))
        require(chatData == null || chatData.toByteArray(StandardCharsets.UTF_8).size <= 1_024)
    }

    private companion object {
        const val MAX_HTML_BYTES = 32_768
        const val MAX_COMPRESSED_HTML_BYTES = 4_096
        const val MAX_CAPTION_LENGTH = 1_024

        fun compressedSize(bytes: ByteArray): Int {
            val output = ByteArrayOutputStream()
            DeflaterOutputStream(output, Deflater(Deflater.BEST_COMPRESSION)).use { stream ->
                stream.write(bytes)
            }
            return output.size()
        }
    }
}
