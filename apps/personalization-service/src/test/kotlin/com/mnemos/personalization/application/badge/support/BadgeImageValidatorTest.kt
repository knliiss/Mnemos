package com.mnemos.personalization.application.badge.support

import com.mnemos.personalization.application.badge.exception.InvalidBadgeImageException
import com.mnemos.personalization.application.badge.model.BadgeImageUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class BadgeImageValidatorTest {
    private val validator = BadgeImageValidator()

    @Test
    fun `accepts square PNG within configured bounds`() {
        val bytes = pngBytes(width = 128, height = 128)

        val result =
            validator.validate(
                BadgeImageUpload(
                    bytes = bytes,
                    contentType = "image/png",
                ),
            )

        assertEquals("image/png", result.contentType)
        assertEquals(bytes.toList(), result.bytes.toList())
    }

    @Test
    fun `rejects non square PNG`() {
        val bytes = pngBytes(width = 128, height = 64)

        assertThrows(InvalidBadgeImageException::class.java) {
            validator.validate(
                BadgeImageUpload(
                    bytes = bytes,
                    contentType = "image/png",
                ),
            )
        }
    }

    @Test
    fun `rejects spoofed PNG content type`() {
        assertThrows(InvalidBadgeImageException::class.java) {
            validator.validate(
                BadgeImageUpload(
                    bytes = "not-a-png".encodeToByteArray(),
                    contentType = "image/png",
                ),
            )
        }
    }

    @Test
    fun `rejects oversized image payload before decoding`() {
        val bytes = ByteArray(BadgeImageValidator.MAX_FILE_SIZE_BYTES + 1)

        assertThrows(InvalidBadgeImageException::class.java) {
            validator.validate(
                BadgeImageUpload(
                    bytes = bytes,
                    contentType = "image/png",
                ),
            )
        }
    }

    private fun pngBytes(
        width: Int,
        height: Int,
    ): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val output = ByteArrayOutputStream()

        ImageIO.write(image, "png", output)

        return output.toByteArray()
    }
}
