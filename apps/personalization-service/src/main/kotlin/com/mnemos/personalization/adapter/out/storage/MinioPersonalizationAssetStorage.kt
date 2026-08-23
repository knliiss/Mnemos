package com.mnemos.personalization.adapter.out.storage

import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage
import com.mnemos.personalization.application.asset.port.out.PersonalizationAssetStorage
import com.mnemos.personalization.configuration.storage.PersonalizationStorageProperties
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.errors.ErrorResponseException
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class MinioPersonalizationAssetStorage(
    private val client: MinioClient,
    private val properties: PersonalizationStorageProperties,
) : PersonalizationAssetStorage {
    override fun store(
        objectKey: String,
        image: PersonalizationAssetImage,
    ) {
        ByteArrayInputStream(image.bytes).use { input ->
            client.putObject(
                PutObjectArgs
                    .builder()
                    .bucket(properties.bucket)
                    .`object`(objectKey)
                    .contentType(image.contentType)
                    .stream(
                        input,
                        image.bytes.size.toLong(),
                        -1,
                    ).build(),
            )
        }
    }

    override fun load(objectKey: String): PersonalizationAssetImage? =
        try {
            client
                .getObject(
                    GetObjectArgs
                        .builder()
                        .bucket(properties.bucket)
                        .`object`(objectKey)
                        .build(),
                ).use { response ->
                    val bytes = response.readAllBytes()
                    val decoded =
                        ByteArrayInputStream(bytes).use { input ->
                            ImageIO.read(input)
                        } ?: return null

                    PersonalizationAssetImage(
                        bytes = bytes,
                        contentType = response.headers()["Content-Type"] ?: DEFAULT_CONTENT_TYPE,
                        widthPx = decoded.width,
                        heightPx = decoded.height,
                    )
                }
        } catch (exception: ErrorResponseException) {
            when (exception.errorResponse().code()) {
                NO_SUCH_KEY -> null
                else -> throw exception
            }
        }

    override fun delete(objectKey: String) {
        client.removeObject(
            RemoveObjectArgs
                .builder()
                .bucket(properties.bucket)
                .`object`(objectKey)
                .build(),
        )
    }

    companion object {
        private const val DEFAULT_CONTENT_TYPE = "image/png"
        private const val NO_SUCH_KEY = "NoSuchKey"
    }
}
