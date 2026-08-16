package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.adapter.inbound.http.accountreaction.AccountReactionApiMapper.toResponse
import com.mnemos.social.application.reaction.AccountReactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/reaction")
class AccountReactionController(
    private val service: AccountReactionService,
) {
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun set(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: SetAccountReactionRequest,
    ) {
        service.set(accountId, request.type)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@PathVariable accountId: UUID) {
        service.remove(accountId)
    }

    @GetMapping
    fun current(@PathVariable accountId: UUID): AccountReactionStateResponse =
        AccountReactionStateResponse(
            type = service.current(accountId),
        )

    @GetMapping("/summary")
    fun summary(@PathVariable accountId: UUID): AccountReactionSummaryResponse =
        service
            .summary(accountId)
            .toResponse()
}
