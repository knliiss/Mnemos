package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.common.web.logging.MnemosHeaders.IDEMPOTENCY_KEY
import com.mnemos.moderation.adapter.inbound.http.admin.wallet.WalletAdjustmentApiMapper.toCommand
import com.mnemos.moderation.adapter.inbound.http.admin.wallet.WalletAdjustmentApiMapper.toQuery
import com.mnemos.moderation.adapter.inbound.http.admin.wallet.WalletAdjustmentApiMapper.toResponse
import com.mnemos.moderation.application.wallet.usecase.create.CreateWalletAdjustmentService
import com.mnemos.moderation.application.wallet.usecase.query.WalletAdjustmentQueryService
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/moderation/wallet-adjustments")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminWalletAdjustmentController(
    private val createService: CreateWalletAdjustmentService,
    private val queryService: WalletAdjustmentQueryService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @RequestBody
        @Valid
        request: CreateWalletAdjustmentRequest,
    ): WalletAdjustmentResponse =
        createService
            .execute(
                request.toCommand(idempotencyKey),
            ).toResponse()

    @GetMapping("/{operationId}")
    fun get(
        @PathVariable
        operationId: UUID,
    ): WalletAdjustmentResponse = queryService.get(WalletAdjustmentOperationId(operationId)).toResponse()

    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListWalletAdjustmentsRequest,
    ): List<WalletAdjustmentResponse> =
        queryService
            .list(request.toQuery())
            .map { operation -> operation.toResponse() }
}
