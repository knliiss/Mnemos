package com.mnemos.personalization.adapter.out.storage

import com.mnemos.personalization.application.badge.model.BadgeImageContent
import com.mnemos.personalization.application.badge.port.out.BadgeImageStorage
import com.mnemos.personalization.configuration.storage.PersonalizationStorageProperties
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.errors.ErrorResponseException
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class MinioBadgeImageStorage(
    private val client: MinioClient,
    private val properties: PersonalizationStorageProperties,
) : BadgeImageStorage {
    @Volatile
    private var bucketReady = false

    private val bucketLock = Any()

    override fun store(
        objectKey: String,
        image: BadgeImageContent,
    ) {
        ensureBucket()

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

    override fun load(objectKey: String): BadgeImageContent? {
        ensureBucket()

        return try {
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
            if (exception.errorResponse().code() == NO_SUCH_KEY) {
                null
            } else {
                throw exception
            }
        }
    }

    override fun delete(objectKey: String) {
        ensureBucket()

        client.removeObject(
            RemoveObjectArgs
                .builder()
                .bucket(properties.bucket)
                .`object`(objectKey)
                .build(),
        )
    }

    private fun ensureBucket() {
        if (bucketReady) {
            return
        }

        synchronized(bucketLock) {
            if (bucketReady) {
                return
            }

            val exists =
                client.bucketExists(
                    BucketExistsArgs
                        .builder()
                        .bucket(properties.bucket)
                        .build(),
                )

            if (!exists) {
                client.makeBucket(
                    MakeBucketArgs
                        .builder()
                        .bucket(properties.bucket)
                        .build(),
                )
            }

            bucketReady = true
        }
    }

    companion object {
        private const val DEFAULT_CONTENT_TYPE = "application/octet-stream"
        private const val NO_SUCH_KEY = "NoSuchKey"
    }
}
