package com.mnemos.personalization.adapter.inbound.http.asset

import com.mnemos.personalization.application.asset.usecase.GetPersonalizationAssetService
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.UUID

@RestController
@RequestMapping("/api/v1/personalizations/assets")
class PersonalizationAssetContentController(
    private val service: GetPersonalizationAssetService,
) {
    @GetMapping("/{assetId}/content")
    fun content(
        @PathVariable assetId: UUID,
    ): ResponseEntity<ByteArray> {
        val image = service.load(PersonalizationAssetId(assetId)) ?: return ResponseEntity.notFound().build()

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(image.contentType))
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
            .body(image.bytes)
    }
}
