package com.mnemos.personalization.application.asset.support

import com.mnemos.personalization.application.asset.exception.InvalidPersonalizationAssetException
import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class PersonalizationAssetImageValidator {
    fun validate(
        bytes: ByteArray,
        contentType: String?,
    ): PersonalizationAssetImage {
        if (bytes.isEmpty()) {
            throw InvalidPersonalizationAssetException("Personalization asset cannot be empty")
        }

        if (bytes.size > MAX_FILE_SIZE_BYTES) {
            throw InvalidPersonalizationAssetException("Personalization asset must not exceed 2 MiB")
        }

        if (contentType != null && contentType != PNG_CONTENT_TYPE) {
            throw InvalidPersonalizationAssetException("Personalization asset must use image/png content type")
        }

        val image =
            ByteArrayInputStream(bytes).use { input ->
                javax.imageio.ImageIO.read(input)
            } ?: throw InvalidPersonalizationAssetException("Personalization asset must be a valid PNG image")

        if (image.width !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) {
            throw InvalidPersonalizationAssetException("Personalization asset width must be between 16 and 2048 pixels")
        }

        if (image.height !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) {
            throw InvalidPersonalizationAssetException("Personalization asset height must be between 16 and 2048 pixels")
        }

        return PersonalizationAssetImage(
            bytes = bytes.copyOf(),
            contentType = PNG_CONTENT_TYPE,
            widthPx = image.width,
            heightPx = image.height,
        )
    }

    companion object {
        const val PNG_CONTENT_TYPE = "image/png"
        const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024
        const val MIN_DIMENSION_PX = 16
        const val MAX_DIMENSION_PX = 2048
    }
}
