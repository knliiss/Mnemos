package com.mnemos.personalization.application.badge.support

import com.mnemos.personalization.application.badge.exception.InvalidBadgeImageException
import com.mnemos.personalization.application.badge.model.BadgeImageContent
import com.mnemos.personalization.application.badge.model.BadgeImageUpload
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

@Component
class BadgeImageValidator {
    fun validate(upload: BadgeImageUpload): BadgeImageContent {
        val bytes = upload.bytes

        if (bytes.isEmpty()) {
            throw InvalidBadgeImageException("Badge image cannot be empty")
        }

        if (bytes.size > MAX_FILE_SIZE_BYTES) {
            throw InvalidBadgeImageException("Badge image must not exceed 256 KiB")
        }

        if (upload.contentType != null && upload.contentType != PNG_CONTENT_TYPE) {
            throw InvalidBadgeImageException("Badge image must use image/png content type")
        }

        if (!hasPngSignature(bytes)) {
            throw InvalidBadgeImageException("Badge image must be a valid PNG file")
        }

        val width = readPngDimension(bytes, WIDTH_OFFSET)
        val height = readPngDimension(bytes, HEIGHT_OFFSET)

        if (width != height) {
            throw InvalidBadgeImageException("Badge image must be square")
        }

        if (width !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) {
            throw InvalidBadgeImageException("Badge image dimensions must be between 64x64 and 512x512")
        }

        val decoded =
            ByteArrayInputStream(bytes).use { input ->
                ImageIO.read(input)
            } ?: throw InvalidBadgeImageException("Badge image PNG data cannot be decoded")

        if (decoded.width != width || decoded.height != height) {
            throw InvalidBadgeImageException("Badge image dimensions are inconsistent")
        }

        return BadgeImageContent(
            bytes = bytes.copyOf(),
            contentType = PNG_CONTENT_TYPE,
        )
    }

    private fun hasPngSignature(bytes: ByteArray): Boolean {
        if (bytes.size < PNG_HEADER_MIN_SIZE) {
            return false
        }

        return PNG_SIGNATURE.indices.all { index -> bytes[index] == PNG_SIGNATURE[index] }
    }

    private fun readPngDimension(
        bytes: ByteArray,
        offset: Int,
    ): Int =
        ByteBuffer
            .wrap(bytes, offset, Int.SIZE_BYTES)
            .order(ByteOrder.BIG_ENDIAN)
            .int

    companion object {
        const val PNG_CONTENT_TYPE = "image/png"
        const val MAX_FILE_SIZE_BYTES = 256 * 1024
        const val MIN_DIMENSION_PX = 64
        const val MAX_DIMENSION_PX = 512

        private const val PNG_HEADER_MIN_SIZE = 24
        private const val WIDTH_OFFSET = 16
        private const val HEIGHT_OFFSET = 20
        private val PNG_SIGNATURE =
            byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
            )
    }
}
