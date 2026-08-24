package com.mnemos.personalization.application.asset.support

import com.mnemos.personalization.application.asset.exception.InvalidPersonalizationAssetException
import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

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

        if (!bytes.hasPngSignature()) {
            throw InvalidPersonalizationAssetException("Personalization asset must be a valid PNG image")
        }

        val dimensions = bytes.readPngDimensions()

        validateDimensions(dimensions)

        val image =
            ByteArrayInputStream(bytes).use { input ->
                ImageIO.read(input)
            } ?: throw InvalidPersonalizationAssetException("Personalization asset must be a valid PNG image")

        if (image.width != dimensions.widthPx || image.height != dimensions.heightPx) {
            throw InvalidPersonalizationAssetException("Personalization asset PNG dimensions are inconsistent")
        }

        return PersonalizationAssetImage(
            bytes = bytes.copyOf(),
            contentType = PNG_CONTENT_TYPE,
            widthPx = dimensions.widthPx,
            heightPx = dimensions.heightPx,
        )
    }

    private fun validateDimensions(dimensions: PngDimensions) {
        if (dimensions.widthPx !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) {
            throw InvalidPersonalizationAssetException("Personalization asset width must be between 16 and 2048 pixels")
        }

        if (dimensions.heightPx !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) {
            throw InvalidPersonalizationAssetException("Personalization asset height must be between 16 and 2048 pixels")
        }
    }

    private fun ByteArray.readPngDimensions(): PngDimensions {
        if (size < PNG_IHDR_REQUIRED_BYTES) {
            throw InvalidPersonalizationAssetException("Personalization asset must contain a valid PNG IHDR chunk")
        }

        val header = ByteBuffer.wrap(this)
        val chunkLength = header.getInt(PNG_IHDR_LENGTH_OFFSET)

        if (chunkLength != PNG_IHDR_DATA_LENGTH || !hasPngIhdrType()) {
            throw InvalidPersonalizationAssetException("Personalization asset must contain a valid PNG IHDR chunk")
        }

        return PngDimensions(
            widthPx = header.getInt(PNG_WIDTH_OFFSET),
            heightPx = header.getInt(PNG_HEIGHT_OFFSET),
        )
    }

    private fun ByteArray.hasPngSignature(): Boolean {
        if (size < PNG_SIGNATURE.size) {
            return false
        }

        return PNG_SIGNATURE.indices.all { index -> this[index] == PNG_SIGNATURE[index] }
    }

    private fun ByteArray.hasPngIhdrType(): Boolean =
        PNG_IHDR_TYPE.indices.all { index ->
            this[PNG_IHDR_TYPE_OFFSET + index] == PNG_IHDR_TYPE[index]
        }

    private data class PngDimensions(
        val widthPx: Int,
        val heightPx: Int,
    )

    companion object {
        const val PNG_CONTENT_TYPE = "image/png"
        const val MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024
        const val MIN_DIMENSION_PX = 16
        const val MAX_DIMENSION_PX = 2048

        private const val PNG_IHDR_LENGTH_OFFSET = 8
        private const val PNG_IHDR_TYPE_OFFSET = 12
        private const val PNG_WIDTH_OFFSET = 16
        private const val PNG_HEIGHT_OFFSET = 20
        private const val PNG_IHDR_DATA_LENGTH = 13
        private const val PNG_IHDR_REQUIRED_BYTES = 24

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

        private val PNG_IHDR_TYPE =
            byteArrayOf(
                'I'.code.toByte(),
                'H'.code.toByte(),
                'D'.code.toByte(),
                'R'.code.toByte(),
            )
    }
}
