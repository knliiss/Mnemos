package com.mnemos.personalization.adapter.inbound.http.asset

import com.mnemos.personalization.adapter.inbound.http.asset.PersonalizationAssetApiMapper.toResponse
import com.mnemos.personalization.application.asset.usecase.ManagePersonalizationAssetService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/personalizations/creator/assets")
@PreAuthorize("hasAnyRole('PERSONALIZATION_CREATOR', 'ADMIN', 'SUPER_ADMIN')")
class CreatorPersonalizationAssetController(
    private val service: ManagePersonalizationAssetService,
) {
    @GetMapping
    fun listMine(): List<PersonalizationAssetResponse> =
        service.listMine().map { asset -> asset.toResponse() }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @RequestPart("file") file: MultipartFile,
    ): PersonalizationAssetResponse =
        service
            .upload(
                bytes = file.bytes,
                contentType = file.contentType,
            ).toResponse()
}
