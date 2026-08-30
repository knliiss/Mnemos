package com.mnemos.playerdirectory.adapter.inbound.http.internal

import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmSnapshotResponse
import com.mnemos.playerdirectory.adapter.inbound.http.internal.InternalSaoRealmApiMapper.toInternalResponse
import com.mnemos.playerdirectory.application.realm.usecase.get.GetSaoRealmSnapshotUseCase
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/realms")
class InternalSaoRealmController(
    private val getSaoRealmSnapshotUseCase: GetSaoRealmSnapshotUseCase,
) {
    @GetMapping("/sao")
    @PreAuthorize("authentication.name == 'telegram-bot-service'")
    fun getSaoSnapshot(): InternalSaoRealmSnapshotResponse = getSaoRealmSnapshotUseCase.execute().toInternalResponse()
}
