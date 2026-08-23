package com.mnemos.personalization.adapter.out.storage

import com.mnemos.personalization.application.badge.model.BadgeImageContent
import com.mnemos.personalization.application.badge.port.out.BadgeImageStorage
import com.mnemos.personalization.configuration.storage.PersonalizationStorageProperties
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.errors.ErrorResponseException
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class MinioBadgeImageStorage(
    private val client: MinioClient,
    private val properties: PersonalizationStorageProperties,
) : BadgeImageStorage {
    @PostConstruct
    fun verifyBucketAccess() {
        val exists =
            client.bucketExists(
                BucketExistsArgs
                    .builder()
                    .bucket(properties.bucket)
                    .build(),
            )

        if (!exists) {
            throw IllegalStateException(
                "Personalization object storage bucket '${properties.bucket}' is unavailable",
            )
        }
    }

    override fun store(
        objectKey: String,
        image: BadgeImageContent,
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

    override fun load(objectKey: String): BadgeImageContent? =
        try {
            client
                .getObject(
                    GetObjectArgs
                        .builder()
                        .bucket(properties.bucket)
                        .`object`(objectKey)
                        .build(),
                ).use { response ->
                    BadgeImageContent(
                        bytes = response.readAllBytes(),
                        contentType = response.headers()["Content-Type"] ?: DEFAULT_CONTENT_TYPE,
                    )
                }
        } catch (exception: ErrorResponseException) {
            when (exception.errorResponse().code()) {
                NO_SUCH_KEY -> null
                NO_SUCH_BUCKET ->
                    throw IllegalStateException(
                        "Personalization object storage bucket '${properties.bucket}' disappeared",
                        exception,
                    )

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
        private const val DEFAULT_CONTENT_TYPE = "application/octet-stream"
        private const val NO_SUCH_KEY = "NoSuchKey"
        private const val NO_SUCH_BUCKET = "NoSuchBucket"
    }
}
