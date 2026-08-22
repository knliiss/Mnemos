package com.mnemos.personalization.configuration.storage

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PersonalizationStorageConfiguration {
    @Bean
    fun minioClient(properties: PersonalizationStorageProperties): MinioClient =
        MinioClient
            .builder()
            .endpoint(properties.endpoint)
            .credentials(
                properties.accessKey,
                properties.secretKey,
            ).build()
}
