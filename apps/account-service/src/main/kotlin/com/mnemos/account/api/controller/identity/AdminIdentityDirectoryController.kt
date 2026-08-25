package com.mnemos.account.api.controller.identity

import com.mnemos.account.api.dto.identity.AccountIdentityDirectoryResponse
import com.mnemos.account.api.dto.identity.IdentityDirectoryApiMapper.toAccountResponse
import com.mnemos.account.api.dto.identity.IdentityDirectoryApiMapper.toQuery
import com.mnemos.account.api.dto.identity.IdentityDirectoryApiMapper.toUserResponse
import com.mnemos.account.api.dto.identity.IdentityDirectoryPageResponse
import com.mnemos.account.api.dto.identity.IdentityDirectoryRequest
import com.mnemos.account.api.dto.identity.UserIdentityDirectoryResponse
import com.mnemos.account.application.service.identity.IdentityDirectoryService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/identities")
class AdminIdentityDirectoryController(
    private val identityDirectoryService: IdentityDirectoryService,
) {
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
    fun users(
        @Valid @ModelAttribute request: IdentityDirectoryRequest,
    ): IdentityDirectoryPageResponse<UserIdentityDirectoryResponse> =
        identityDirectoryService
            .searchUsers(request.toQuery())
            .toUserResponse()

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
    fun accounts(
        @Valid @ModelAttribute request: IdentityDirectoryRequest,
    ): IdentityDirectoryPageResponse<AccountIdentityDirectoryResponse> =
        identityDirectoryService
            .searchAccounts(request.toQuery())
            .toAccountResponse()
}
