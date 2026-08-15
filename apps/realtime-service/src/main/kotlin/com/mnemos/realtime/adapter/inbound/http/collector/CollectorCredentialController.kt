package com.mnemos.realtime.adapter.inbound.http.collector

import com.mnemos.realtime.adapter.inbound.http.collector.CollectorCredentialApiMapper.toResponse
import com.mnemos.realtime.application.collector.credential.usecase.create.CreateCollectorCredentialCommand
import com.mnemos.realtime.application.collector.credential.usecase.create.CreateCollectorCredentialUseCase
import com.mnemos.realtime.application.collector.credential.usecase.disable.DisableCollectorCredentialUseCase
import com.mnemos.realtime.application.collector.credential.usecase.enable.EnableCollectorCredentialUseCase
import com.mnemos.realtime.application.collector.credential.usecase.revoke.RevokeCollectorCredentialUseCase
import com.mnemos.realtime.application.collector.credential.usecase.rotate.RotateCollectorCredentialUseCase
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequestMapping("/api/v1/realtime/admin/collectors")
class CollectorCredentialController(
    private val createCredentialUseCase: CreateCollectorCredentialUseCase,
    private val rotateCredentialUseCase: RotateCollectorCredentialUseCase,
    private val enableCredentialUseCase: EnableCollectorCredentialUseCase,
    private val disableCredentialUseCase: DisableCollectorCredentialUseCase,
    private val revokeCredentialUseCase: RevokeCollectorCredentialUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody
        @Valid
        request: CreateCollectorCredentialRequest,
    ): CollectorCredentialResponse =
        createCredentialUseCase
            .execute(
                CreateCollectorCredentialCommand(
                    name = request.name,
                ),
            ).toResponse()

    @PostMapping("/{id}/rotate")
    fun rotate(
        @PathVariable
        id: UUID,
    ): CollectorCredentialResponse =
        rotateCredentialUseCase
            .execute(CollectorCredentialId(id))
            .toResponse()

    @PostMapping("/{id}/enable")
    fun enable(
        @PathVariable
        id: UUID,
    ): CollectorCredentialResponse =
        enableCredentialUseCase
            .execute(CollectorCredentialId(id))
            .toResponse()

    @PostMapping("/{id}/disable")
    fun disable(
        @PathVariable
        id: UUID,
    ): CollectorCredentialResponse =
        disableCredentialUseCase
            .execute(CollectorCredentialId(id))
            .toResponse()

    @DeleteMapping("/{id}")
    fun revoke(
        @PathVariable
        id: UUID,
    ): CollectorCredentialResponse =
        revokeCredentialUseCase
            .execute(CollectorCredentialId(id))
            .toResponse()
}
