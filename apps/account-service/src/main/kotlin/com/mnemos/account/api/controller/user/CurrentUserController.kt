package com.mnemos.account.api.controller.user

import com.mnemos.account.api.dto.user.UpdateLocalizationRequest
import com.mnemos.account.api.dto.user.UpdateUsernameRequest
import com.mnemos.account.api.dto.user.UserApiMapper.toResponse
import com.mnemos.account.api.dto.user.UserResponse
import com.mnemos.account.application.service.user.UserService
import com.mnemos.common.core.model.user.Username
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class CurrentUserController(
    private val userService: UserService,
) {
    @PutMapping("/me")
    fun initializeCurrentUser(): UserResponse =
        userService
            .initializeCurrentUser()
            .toResponse()

    @GetMapping("/me")
    fun getCurrentUser(): UserResponse =
        userService
            .getCurrentUser()
            .toResponse()

    @PatchMapping("/me/username")
    fun updateUsername(
        @RequestBody @Valid request: UpdateUsernameRequest,
    ): UserResponse =
        userService
            .updateUsername(
                Username(request.username),
            ).toResponse()

    @PatchMapping("/me/localization")
    fun updateLocalization(
        @RequestBody @Valid request: UpdateLocalizationRequest,
    ): UserResponse =
        userService
            .updateLocalization(
                request.localizationLanguage,
            ).toResponse()
}
