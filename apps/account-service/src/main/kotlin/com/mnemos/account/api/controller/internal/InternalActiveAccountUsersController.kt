package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.service.user.InternalActiveAccountUsersService
import com.mnemos.contracts.account.internal.v1.InternalActiveAccountUserResponse
import com.mnemos.contracts.account.internal.v1.InternalActiveAccountUsersResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/notification/active-account-users")
@PreAuthorize("authentication.name == 'notification-service'")
class InternalActiveAccountUsersController(
    private val service: InternalActiveAccountUsersService,
) {
    @GetMapping
    fun find(
        @RequestParam nickname: String,
    ): InternalActiveAccountUsersResponse =
        InternalActiveAccountUsersResponse(
            users =
                service
                    .findByNickname(nickname)
                    .map { user ->
                        InternalActiveAccountUserResponse(
                            userId = user.id.id,
                            telegramId = user.telegramId.id,
                        )
                    },
        )
}
