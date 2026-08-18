package com.mnemos.account.api.controller.context

import com.mnemos.account.api.dto.account.AccountApiMapper.toResponse
import com.mnemos.account.api.dto.context.CurrentContextResponse
import com.mnemos.account.api.dto.user.UserApiMapper.toResponse
import com.mnemos.account.application.service.account.AccountQueryService
import com.mnemos.account.application.service.user.UserService
import com.mnemos.security.CurrentMnemosUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me")
class CurrentContextController(
    private val userService: UserService,
    private val accountQueryService: AccountQueryService,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @GetMapping("/context")
    fun getContext(): CurrentContextResponse {
        val user = userService.getCurrentUser()
        val accountContext = accountQueryService.getCurrentContext()
        val principal = currentMnemosUser.get()

        return CurrentContextResponse(
            user = user.toResponse(),
            systemRoles = principal.roles,
            availableAccounts = accountContext.availableAccounts.map { account -> account.toResponse() },
            activeAccount = accountContext.activeAccount?.toResponse(),
            effectivePermissions = accountContext.effectivePermissions,
        )
    }
}
