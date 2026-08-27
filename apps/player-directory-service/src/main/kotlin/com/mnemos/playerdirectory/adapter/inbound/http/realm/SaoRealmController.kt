package com.mnemos.playerdirectory.adapter.inbound.http.realm

import com.mnemos.playerdirectory.adapter.inbound.http.realm.SaoRealmApiMapper.toResponse
import com.mnemos.playerdirectory.application.realm.usecase.get.GetSaoRealmSnapshotUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/realms")
class SaoRealmController(
    private val getSaoRealmSnapshotUseCase: GetSaoRealmSnapshotUseCase,
) {
    @GetMapping("/sao")
    fun getSaoRealms(): SaoRealmSnapshotResponse = getSaoRealmSnapshotUseCase.execute().toResponse()
}
