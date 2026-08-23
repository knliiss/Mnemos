package com.mnemos.moderation.adapter.inbound.http.admin.sanction

import com.mnemos.moderation.adapter.inbound.http.admin.sanction.SanctionApiMapper.toCommand
import com.mnemos.moderation.adapter.inbound.http.admin.sanction.SanctionApiMapper.toQuery
import com.mnemos.moderation.adapter.inbound.http.admin.sanction.SanctionApiMapper.toResponse
import com.mnemos.moderation.application.sanction.usecase.create.CreateSanctionUseCase
import com.mnemos.moderation.application.sanction.usecase.list.ListSanctionsUseCase
import com.mnemos.moderation.application.sanction.usecase.revoke.RevokeSanctionCommand
import com.mnemos.moderation.application.sanction.usecase.revoke.RevokeSanctionUseCase
import com.mnemos.moderation.domain.sanction.model.SanctionId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/moderation/sanctions")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
class AdminSanctionController(
    private val createSanctionUseCase: CreateSanctionUseCase,
    private val revokeSanctionUseCase: RevokeSanctionUseCase,
    private val listSanctionsUseCase: ListSanctionsUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody
        @Valid
        request: CreateSanctionRequest,
    ): SanctionResponse {
        val command = request.toCommand()
        val result = createSanctionUseCase.execute(command)

        return result.sanction.toResponse()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revoke(
        @PathVariable
        id: UUID,
    ) {
        revokeSanctionUseCase.execute(
            RevokeSanctionCommand(
                sanctionId = SanctionId(id),
            ),
        )
    }

    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListSanctionsRequest,
    ): List<SanctionResponse> =
        listSanctionsUseCase
            .execute(request.toQuery())
            .map { sanction -> sanction.toResponse() }
}
