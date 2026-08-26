package com.mnemos.personalization.adapter.inbound.http.internal.profileappearance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.adapter.inbound.http.internal.profileappearance.InternalProfileAppearanceApiMapper.toResponse
import com.mnemos.personalization.application.ownership.usecase.getactiveappearance.GetActiveProfileAppearanceUseCase
import com.mnemos.personalization.contracts.internal.v1.InternalProfileAppearanceResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/accounts/{accountId}/profile-appearance")
@PreAuthorize("authentication.name == 'profile-service'")
class InternalProfileAppearanceController(
    private val getActiveProfileAppearanceUseCase: GetActiveProfileAppearanceUseCase,
) {
    @GetMapping
    fun get(
        @PathVariable accountId: UUID,
    ): InternalProfileAppearanceResponse =
        getActiveProfileAppearanceUseCase.execute(AccountId(accountId)).toResponse()
}
