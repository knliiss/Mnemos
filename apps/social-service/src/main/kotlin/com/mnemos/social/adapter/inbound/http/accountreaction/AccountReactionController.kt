package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.application.reaction.AccountReactionService
import com.mnemos.social.application.reaction.AccountReactionSummary
import com.mnemos.social.domain.reaction.model.AccountReactionType
import jakarta.validation.constraints.NotNull
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

data class SetAccountReactionRequest(
    @field:NotNull
    val type: AccountReactionType,
)

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/reaction")
class AccountReactionController(
    private val service: AccountReactionService,
) {
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun set(
        @PathVariable accountId: UUID,
        @RequestBody request: SetAccountReactionRequest,
    ) {
        service.set(accountId, request.type)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@PathVariable accountId: UUID) {
        service.remove(accountId)
    }

    @GetMapping("/summary")
    fun summary(@PathVariable accountId: UUID): AccountReactionSummary = service.summary(accountId)
}
