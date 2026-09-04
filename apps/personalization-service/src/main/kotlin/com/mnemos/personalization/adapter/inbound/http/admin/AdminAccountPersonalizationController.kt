package com.mnemos.personalization.adapter.inbound.http.admin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.adapter.inbound.http.ownership.OwnedPersonalizationResponse
import com.mnemos.personalization.adapter.inbound.http.ownership.PersonalizationOwnershipApiMapper.toResponse
import com.mnemos.personalization.application.ownership.usecase.admin.AdminAccountPersonalizationService
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts/{accountId}/personalizations")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminAccountPersonalizationController(
    private val adminAccountPersonalizationService: AdminAccountPersonalizationService,
) {
    @GetMapping
    fun list(
        @PathVariable accountId: UUID,
    ): List<OwnedPersonalizationResponse> =
        adminAccountPersonalizationService
            .list(AccountId(accountId))
            .map { ownership -> ownership.toResponse() }

    @PutMapping("/{personalizationId}")
    fun grant(
        @PathVariable accountId: UUID,
        @PathVariable personalizationId: UUID,
    ): OwnedPersonalizationResponse =
        adminAccountPersonalizationService
            .grant(
                accountId = AccountId(accountId),
                personalizationId = PersonalizationId(personalizationId),
            ).toResponse()

    @PutMapping("/{personalizationId}/active")
    fun activate(
        @PathVariable accountId: UUID,
        @PathVariable personalizationId: UUID,
    ): OwnedPersonalizationResponse =
        adminAccountPersonalizationService
            .activate(
                accountId = AccountId(accountId),
                personalizationId = PersonalizationId(personalizationId),
            ).toResponse()

    @DeleteMapping("/{personalizationId}/active")
    fun deactivate(
        @PathVariable accountId: UUID,
        @PathVariable personalizationId: UUID,
    ): OwnedPersonalizationResponse =
        adminAccountPersonalizationService
            .deactivate(
                accountId = AccountId(accountId),
                personalizationId = PersonalizationId(personalizationId),
            ).toResponse()

    @DeleteMapping("/{personalizationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revoke(
        @PathVariable accountId: UUID,
        @PathVariable personalizationId: UUID,
    ) {
        adminAccountPersonalizationService.revoke(
            accountId = AccountId(accountId),
            personalizationId = PersonalizationId(personalizationId),
        )
    }
}
