package com.mnemos.account.api.controller.internal

import com.mnemos.account.api.controller.internal.InternalAccountApiMapper.toBootstrapResponse
import com.mnemos.account.application.service.account.InternalAccountQueryService
import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/accounts/bootstrap")
class InternalAccountBootstrapController(
    private val queryService: InternalAccountQueryService,
) {
    @GetMapping
    fun bootstrap(
        @RequestParam(required = false) afterAccountId: UUID?,
        @RequestParam(defaultValue = "200") limit: Int,
    ): List<InternalAccountBootstrapResponse> =
        queryService
            .findBootstrapBatch(
                afterAccountId = afterAccountId,
                limit = limit,
            ).map { account -> account.toBootstrapResponse() }
}
