package com.mnemos.personalization.application.asset.support

import com.mnemos.personalization.application.asset.exception.InvalidPersonalizationAssetException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

class PersonalizationAssetImageValidatorTest {
    private val validator = PersonalizationAssetImageValidator()

    @Test
    fun `accepts valid png image`() {
        val bytes = imageBytes(format = "png")

        val result =
            validator.validate(
                bytes = bytes,
                contentType = PersonalizationAssetImageValidator.PNG_CONTENT_TYPE,
            )

        assertEquals(PersonalizationAssetImageValidator.PNG_CONTENT_TYPE, result.contentType)
        assertEquals(32, result.widthPx)
        assertEquals(24, result.heightPx)
    }

    @Test
    fun `rejects non png image even when content type is missing`() {
        val bytes = imageBytes(format = "jpg")

        assertThrows(InvalidPersonalizationAssetException::class.java) {
            validator.validate(
                bytes = bytes,
                contentType = null,
            )
        }
    }

    @Test
    fun `rejects oversized declared dimensions before image decode`() {
        val bytes = imageBytes(format = "png")
        ByteBuffer.wrap(bytes).putInt(PNG_WIDTH_OFFSET, 100_000)

        val exception =
            assertThrows(InvalidPersonalizationAssetException::class.java) {
                validator.validate(
                    bytes = bytes,
                    contentType = PersonalizationAssetImageValidator.PNG_CONTENT_TYPE,
                )
            }

        assertEquals(
            "Personalization asset width must be between 16 and 2048 pixels",
            exception.message,
        )
    }

    private fun imageBytes(format: String): ByteArray {
        val image = BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB)

        return ByteArrayOutputStream().use { output ->
            check(ImageIO.write(image, format, output))
            output.toByteArray()
        }
    }

    companion object {
        private const val PNG_WIDTH_OFFSET = 16
    }
}
