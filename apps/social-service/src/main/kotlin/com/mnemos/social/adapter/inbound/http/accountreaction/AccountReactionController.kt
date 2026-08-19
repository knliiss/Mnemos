package com.mnemos.social.adapter.inbound.http.accountreaction

import com.mnemos.social.adapter.inbound.http.accountreaction.AccountReactionApiMapper.toCommand
import com.mnemos.social.adapter.inbound.http.accountreaction.AccountReactionApiMapper.toResponse
import com.mnemos.social.application.accountreaction.usecase.get.GetAccountReactionUseCase
import com.mnemos.social.application.accountreaction.usecase.remove.RemoveAccountReactionUseCase
import com.mnemos.social.application.accountreaction.usecase.set.SetAccountReactionUseCase
import com.mnemos.social.application.accountreaction.usecase.summary.GetAccountReactionSummaryUseCase
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
    private val setAccountReactionUseCase: SetAccountReactionUseCase,
    private val removeAccountReactionUseCase: RemoveAccountReactionUseCase,
    private val getAccountReactionUseCase: GetAccountReactionUseCase,
    private val getAccountReactionSummaryUseCase: GetAccountReactionSummaryUseCase,
) {
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun set(
        @PathVariable
        accountId: UUID,
        @Valid @RequestBody request: SetAccountReactionRequest,
    ) {
        setAccountReactionUseCase.execute(request.toCommand(accountId))
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(
        @PathVariable
        accountId: UUID,
    ) {
        removeAccountReactionUseCase.execute(accountId)
    }

    @GetMapping
    fun current(
        @PathVariable
        accountId: UUID,
    ): AccountReactionStateResponse =
        AccountReactionStateResponse(
            type = getAccountReactionUseCase.execute(accountId),
        )

    @GetMapping("/summary")
    fun summary(
        @PathVariable
        accountId: UUID,
    ): AccountReactionSummaryResponse =
        getAccountReactionSummaryUseCase
            .execute(accountId)
            .toResponse()
}
