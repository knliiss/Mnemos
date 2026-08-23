package com.mnemos.personalization.application.asset.support

import com.mnemos.personalization.application.asset.exception.InvalidPersonalizationAssetException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
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

    private fun imageBytes(format: String): ByteArray {
        val image = BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB)

        return ByteArrayOutputStream().use { output ->
            check(ImageIO.write(image, format, output))
            output.toByteArray()
        }
    }
}
