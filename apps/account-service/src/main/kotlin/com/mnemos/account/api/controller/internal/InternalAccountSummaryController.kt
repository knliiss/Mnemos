package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.account.AccountLookupSummary
import com.mnemos.account.application.service.account.GetInternalAccountSummariesService
import com.mnemos.contracts.account.internal.v1.InternalAccountSummariesRequest
import com.mnemos.contracts.account.internal.v1.InternalAccountSummaryResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/accounts/summaries")
@PreAuthorize(
    "authentication.name == 'bet-service' or " +
        "authentication.name == 'wallet-service'",
)
class InternalAccountSummaryController(
    private val service: GetInternalAccountSummariesService,
) {
    @PostMapping
    fun list(
        @RequestBody request: InternalAccountSummariesRequest,
    ): List<InternalAccountSummaryResponse> =
        service
            .execute(request.accountIds)
            .map(AccountLookupSummary::toResponse)

    private fun AccountLookupSummary.toResponse(): InternalAccountSummaryResponse =
        InternalAccountSummaryResponse(
            accountId = id.id,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
        )
}
